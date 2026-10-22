package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class RepelSpell extends Spell {

    public RepelSpell() {
        super("repel", "Repel", "A shockwave of force hurls every creature within 6 blocks away from you.",
                MagicSchool.WARDING, MagicStage.ARCANE_ARTS, 5, 25, 100);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "A wider (7 block), stronger shockwave."
                : "8 blocks, stronger still, and the blast wounds (1.5 hearts).";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 6.0 : tier == 2 ? 7.0 : 8.0;
        double strength = tier == 1 ? 1.6 : tier == 2 ? 2.0 : 2.6;

        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : targets) {
            double dx = player.getX() - target.getX();
            double dz = player.getZ() - target.getZ();
            if (dx * dx + dz * dz < 1.0E-4) dx = 0.01;
            target.knockback(strength, dx, dz);
            target.hurtMarked = true;
            Combos.hurl(target, player);
            if (tier >= 3) target.hurt(player.damageSources().indirectMagic(player, player), 3.0f);
        }

        SpellFx.ring(level, HollowmancyParticles.RING_WARD.get(), player.position().add(0, 0.1, 0), (float) radius);
        SpellFx.ring(level, HollowmancyParticles.RING_WARD.get(), player.position().add(0, 1.0, 0), (float) radius * 0.7F);
        level.sendParticles(HollowmancyParticles.WIND.get(), player.getX(), player.getY() + 0.2, player.getZ(),
                40, radius / 3, 0.1, radius / 3, 0.15);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
        return true;
    }
}