package com.walnutbeach.hollowmancy.archmage;

import com.walnutbeach.hollowmancy.magic.HollowmancyAdvancements;
import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.HuntersMarkSpell;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Legendary forms. When an Archmage casts one school's signature spell at tier IV, something more happens
 * on top of the spell itself:
 *   Spark (General)        Storm Crown:  lightning leaps from you to up to 4 more enemies.
 *   Firebolt (Elemental)   Phoenix Volley: a second, wider wave of fire.
 *   Aegis (Warding)        Bastion:      the ward spreads to every ally within 8 blocks.
 *   Reveal (Divination)    All-Seeing:   every revealed enemy is also Marked, taking 25% more damage.
 *   Wellspring (Nature)    Font of Life: Regeneration II for every friend within 10 blocks, and poisons cleared.
 *   Void Lance (Forbidden) Singularity:  where the lance ends, a rift pulls enemies in for 3 seconds.
 * Every cast also tells the Archmage's aura which school to shine with.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class LegendarySpells {
    public static final int TIER = 4;

    /** Called by Spells.release after a spell is cast. */
    public static void afterCast(ServerPlayer player, Spell spell) {
        ArchmageAura.remember(player, spell.getSchool());
        MagicData data = MagicHelper.get(player);
        if (data.stage() < MagicStage.ARCHMAGE || Spells.getTier(data, spell) < TIER) return;
        boolean fired = switch (spell.getId()) {
            case "spark" -> stormCrown(player);
            case "firebolt" -> phoenixVolley(player);
            case "aegis" -> bastion(player);
            case "reveal" -> allSeeing(player);
            case "wellspring" -> fontOfLife(player);
            case "void_lance" -> singularity(player);
            default -> false;
        };
        if (!fired) return;
        HollowmancyAdvancements.award(player, "legendary_form", spell.getId());
        HollowmancyAdvancements.award(player, "legendary_all", spell.getId());
        player.displayClientMessage(Component.translatable("hollowmancy.legendary.cast",
                Component.translatable("hollowmancy.legendary." + spell.getId())).withStyle(ChatFormatting.GOLD), true);
        SpellFx.flourish(player.serverLevel(), spell.getSchool(), player.position(), 2.5F);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SPELL_LEGENDARY.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    public static boolean isSignature(Spell spell) {
        return switch (spell.getId()) {
            case "spark", "firebolt", "aegis", "reveal", "wellspring", "void_lance" -> true;
            default -> false;
        };
    }

    private static List<LivingEntity> enemiesNear(ServerPlayer player, Vec3 at, double radius) {
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(radius),
                e -> e instanceof Enemy && e.isAlive() && !e.isSpectator());
    }

    // ---------------- Storm Crown ----------------

    private static boolean stormCrown(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 chest = player.position().add(0, player.getBbHeight() * 0.7, 0);
        List<LivingEntity> targets = new ArrayList<>(enemiesNear(player, player.position(), 12.0));
        targets.removeIf(e -> !player.hasLineOfSight(e));
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        if (targets.isEmpty()) return false;
        for (LivingEntity target : targets.subList(0, Math.min(4, targets.size()))) {
            Vec3 at = target.position().add(0, target.getBbHeight() * 0.6, 0);
            SpellFx.beam(level, chest, at, SpellFx.STORM, 0.09F, 8, SpellFx.LIGHTNING);
            target.hurt(player.damageSources().indirectMagic(player, player), 6.0F);
            level.sendParticles(HollowmancyParticles.STORM_SPARK.get(), at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.15);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4F, 1.8F);
        return true;
    }

    // ---------------- Phoenix Volley ----------------

    private static boolean phoenixVolley(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        float[][] spread = {{-36, 0}, {-24, 6}, {24, 6}, {36, 0}, {-12, -8}, {12, -8}};
        for (float[] s : spread) {
            Vec3 dir = look.yRot((float) Math.toRadians(s[0])).xRot((float) Math.toRadians(s[1]));
            SmallFireball fireball = new SmallFireball(level, player, dir);
            fireball.setPos(player.getX() + dir.x, player.getEyeY() - 0.1 + dir.y, player.getZ() + dir.z);
            level.addFreshEntity(fireball);
        }
        Vec3 at = player.getEyePosition().add(look);
        level.sendParticles(HollowmancyParticles.EMBER.get(), at.x, at.y, at.z, 30, 0.5, 0.4, 0.5, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    // ---------------- Bastion ----------------

    private static boolean bastion(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0),
                e -> e != player && e.isAlive() && !(e instanceof Enemy));
        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 15, 1));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 15, 0));
            level.sendParticles(HollowmancyParticles.WARD_GLINT.get(), ally.getX(), ally.getY() + ally.getBbHeight() * 0.6, ally.getZ(), 10, 0.3, 0.4, 0.3, 0.03);
        }
        SpellFx.ring(level, HollowmancyParticles.RING_WARD.get(), player.position().add(0, 0.1, 0), 8.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }

    // ---------------- All-Seeing ----------------

    private static boolean allSeeing(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> enemies = enemiesNear(player, player.position(), 48.0);
        if (enemies.isEmpty()) return false;
        for (LivingEntity enemy : enemies) HuntersMarkSpell.mark(enemy, 20 * 15, 0.25F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.8F);
        return true;
    }

    // ---------------- Font of Life ----------------

    private static boolean fontOfLife(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> friends = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0),
                e -> e.isAlive() && !(e instanceof Enemy));
        for (LivingEntity friend : friends) {
            friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1));
            friend.removeEffect(MobEffects.POISON);
            friend.removeEffect(MobEffects.WITHER);
            level.sendParticles(HollowmancyParticles.HEAL_MOTE.get(), friend.getX(), friend.getY() + friend.getBbHeight() * 0.6, friend.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
        }
        SpellFx.ring(level, HollowmancyParticles.RING_NATURE.get(), player.position().add(0, 0.1, 0), 10.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.7F);
        return true;
    }

    // ---------------- Singularity ----------------

    private record Rift(ServerLevel level, Vec3 at, ServerPlayer caster, long until) {}
    private static final List<Rift> RIFTS = new ArrayList<>();

    private static boolean singularity(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 end = level.clip(new ClipContext(start, start.add(player.getLookAngle().scale(24.0)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getLocation();
        Vec3 at = end.subtract(player.getLookAngle().scale(0.8));
        RIFTS.add(new Rift(level, at, player, level.getGameTime() + 60));
        SpellFx.ring(level, HollowmancyParticles.RING_VOID.get(), at, 3.0F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 1.0F, 1.4F);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RIFTS.isEmpty()) return;
        Iterator<Rift> it = RIFTS.iterator();
        while (it.hasNext()) {
            Rift rift = it.next();
            ServerLevel level = rift.level();
            long now = level.getGameTime();
            if (now >= rift.until() || rift.caster().isRemoved()) {
                level.sendParticles(HollowmancyParticles.VOID_WISP.get(), rift.at().x, rift.at().y, rift.at().z, 30, 0.4, 0.4, 0.4, 0.15);
                it.remove();
                continue;
            }
            double spin = now * 0.5;
            for (int i = 0; i < 3; i++) {
                double a = spin + i * Math.PI * 2 / 3;
                level.sendParticles(HollowmancyParticles.VOID_WISP.get(), rift.at().x + Math.cos(a) * 1.2, rift.at().y + Math.sin(spin * 0.3) * 0.3,
                        rift.at().z + Math.sin(a) * 1.2, 1, 0, 0, 0, 0);
            }
            for (LivingEntity enemy : enemiesNear(rift.caster(), rift.at(), 6.0)) {
                Vec3 pull = rift.at().subtract(enemy.position().add(0, enemy.getBbHeight() / 2, 0));
                if (pull.lengthSqr() > 0.5) {
                    enemy.setDeltaMovement(enemy.getDeltaMovement().scale(0.5).add(pull.normalize().scale(0.25)));
                    enemy.hurtMarked = true;
                }
                if (now % 10 == 0) enemy.hurt(rift.caster().damageSources().indirectMagic(rift.caster(), rift.caster()), 2.0F);
            }
            if (now % 20 == 0) level.playSound(null, rift.at().x, rift.at().y, rift.at().z, SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.8F);
        }
    }

    private LegendarySpells() {}
}
