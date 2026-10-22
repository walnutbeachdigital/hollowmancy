package com.walnutbeach.hollowmancy.spell;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Nature's attack: thorned vines lash out and bind the creature you're looking at, rooting it in place. */
public class BrambleSnareSpell extends Spell {
    private static final double RANGE = 16.0;
    private static final int NATURE_GREEN = 0x6BBF59;

    public BrambleSnareSpell() {
        super("bramble_snare", "Bramble Snare",
                "Thorned vines lash out and bind the creature you look at, rooting it in place and tearing at it.",
                MagicSchool.NATURE, MagicStage.ARCANE_ARTS, 4, 25, 160);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "The vines also catch creatures within 3 blocks of your target."
                : "Roots for longer, tears harder, and poisons what it holds.";
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
        if (hit == null || !(hit.getEntity() instanceof LivingEntity first)) return false;   // nothing to bind: no cost

        List<LivingEntity> bound = new ArrayList<>();
        bound.add(first);
        if (tier >= 2) {
            bound.addAll(level.getEntitiesOfClass(LivingEntity.class, first.getBoundingBox().inflate(3.0),
                    e -> e != first && e != player && e.isAlive() && !(e instanceof Player)));
        }

        int rootTicks = tier >= 3 ? 100 : tier == 2 ? 80 : 60;
        float damage = tier >= 3 ? 6.0F : 4.0F;
        Vec3 hand = start.add(look).add(0, -0.25, 0);
        for (LivingEntity target : bound) {
            Vec3 at = target.position().add(0, target.getBbHeight() * 0.5, 0);
            target.hurt(player.damageSources().indirectMagic(player, player), Combos.shatter(player, target, damage));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, rootTicks, 6, false, true));
            if (tier >= 3) target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            if (target instanceof Mob mob) mob.getNavigation().stop();

            SpellFx.beam(level, target == first ? hand : first.position().add(0, first.getBbHeight() * 0.5, 0), at,
                    NATURE_GREEN, 0.09F, 12, SpellFx.LIGHTNING);
            SpellFx.ring(level, HollowmancyParticles.RING_NATURE.get(), target.position().add(0, 0.08, 0), 1.2F);
            level.sendParticles(HollowmancyParticles.LEAF.get(), at.x, at.y, at.z, 14, 0.35, 0.5, 0.35, 0.02);
            level.sendParticles(HollowmancyParticles.RUNE_NATURE.get(), target.getX(), target.getY() + 0.2, target.getZ(), 3, 0.4, 0.1, 0.4, 0.01);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, first.getX(), first.getY(), first.getZ(), SoundEvents.VINE_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
        return true;
    }
}
