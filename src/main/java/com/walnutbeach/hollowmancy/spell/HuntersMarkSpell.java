package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Divination's attack: mark the creature you look at. It glows, can't hide, and takes extra damage from
 * everything for a while. Glowing also triggers the "Exposed" combo, so magic hits it harder still.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class HuntersMarkSpell extends Spell {
    private static final double RANGE = 32.0;
    private static final Map<LivingEntity, Mark> MARKED = new WeakHashMap<>();

    private record Mark(long until, float bonus) {}

    public HuntersMarkSpell() {
        super("hunters_mark", "Hunter's Mark",
                "Mark the creature you look at. For 10 seconds it glows, cannot turn invisible, and takes 30% more damage from every source.",
                MagicSchool.DIVINATION, MagicStage.ARCANE_ARTS, 4, 20, 200);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "The mark lasts 15 seconds and adds 40% damage."
                : "The mark lasts 20 seconds and adds 50% damage.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = level.clip(new ClipContext(start, start.add(look.scale(RANGE)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getLocation();
        AABB box = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, start, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) return false;

        int seconds = tier >= 3 ? 20 : tier == 2 ? 15 : 10;
        float bonus = tier >= 3 ? 0.5F : tier == 2 ? 0.4F : 0.3F;
        MARKED.put(target, new Mark(level.getGameTime() + seconds * 20L, bonus));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, seconds * 20, 0, false, false));
        target.removeEffect(MobEffects.INVISIBILITY);

        Vec3 at = target.position().add(0, target.getBbHeight() + 0.4, 0);
        SpellFx.beam(level, start.add(look).add(0, -0.2, 0), target.position().add(0, target.getBbHeight() * 0.6, 0),
                SpellFx.DIVINE, 0.05F, 10, SpellFx.SMOOTH);
        SpellFx.runeCircle(level, HollowmancyParticles.RUNE_DIVINE.get(), at, 0.6, 4);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.6F);
        return true;
    }

    /** Mark a creature from elsewhere: the All-Seeing legendary form of Reveal. Keeps the stronger mark. */
    public static void mark(LivingEntity target, int ticks, float bonus) {
        Mark old = MARKED.get(target);
        long until = target.level().getGameTime() + ticks;
        if (old == null || old.until() < until || old.bonus() < bonus) {
            MARKED.put(target, new Mark(Math.max(until, old == null ? 0 : old.until()), Math.max(bonus, old == null ? 0 : old.bonus())));
        }
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
        target.removeEffect(MobEffects.INVISIBILITY);
    }

    public static boolean isMarked(LivingEntity entity) {
        Mark mark = MARKED.get(entity);
        return mark != null && entity.level().getGameTime() < mark.until();
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Mark mark = MARKED.get(event.getEntity());
        if (mark == null) return;
        if (event.getEntity().level().getGameTime() >= mark.until()) {
            MARKED.remove(event.getEntity());
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + mark.bonus()));
        if (event.getEntity().level() instanceof ServerLevel level) {
            LivingEntity e = event.getEntity();
            level.sendParticles(HollowmancyParticles.DIVINE_MOTE.get(), e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    /** A slowly turning ring of glyphs over everything marked, and no hiding. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (MARKED.isEmpty()) return;
        for (Map.Entry<LivingEntity, Mark> entry : new ArrayList<>(MARKED.entrySet())) {
            LivingEntity e = entry.getKey();
            if (!e.isAlive() || !(e.level() instanceof ServerLevel level) || level.getGameTime() >= entry.getValue().until()) {
                MARKED.remove(e);
                continue;
            }
            if (e.hasEffect(MobEffects.INVISIBILITY)) e.removeEffect(MobEffects.INVISIBILITY);
            if (level.getGameTime() % 10 == 0) {
                double a = level.getGameTime() * 0.15;
                level.sendParticles(HollowmancyParticles.RUNE_DIVINE.get(), e.getX() + Math.cos(a) * 0.5, e.getY() + e.getBbHeight() + 0.35,
                        e.getZ() + Math.sin(a) * 0.5, 1, 0, 0, 0, 0);
            }
        }
    }
}
