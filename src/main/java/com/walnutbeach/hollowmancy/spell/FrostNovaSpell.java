package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class FrostNovaSpell extends Spell {

    public FrostNovaSpell() {
        super("frost_nova", "Frost Nova", "Killing cold bursts outward, freezing and slowing every creature within 5 blocks.",
                MagicSchool.ELEMENTAL, MagicStage.ARCANE_ARTS, 5, 35, 200);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Wider (6.5 blocks) and colder: 2 hearts."
                : "8 blocks, 3 hearts, and enemies are frozen nearly solid.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 5.0 : tier == 2 ? 6.5 : 8.0;
        float damage = tier == 1 ? 3.0f : tier == 2 ? 4.0f : 6.0f;
        int slowLevel = tier == 3 ? 5 : 3;
        int slowTicks = tier == 1 ? 100 : tier == 2 ? 120 : 160;

        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive() && entity.distanceToSqr(player) <= radius * radius);

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().indirectMagic(player, player), damage);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowLevel));
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 60));
            Combos.chill(target);
        }

        for (int angle = 0; angle < 360; angle += 12) {
            double rad = Math.toRadians(angle);
            for (double r = 1.0; r <= radius; r += 1.0) {
                level.sendParticles(HollowmancyParticles.FROST_SHARD.get(),
                        player.getX() + Math.cos(rad) * r, player.getY() + 0.3, player.getZ() + Math.sin(rad) * r,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        SpellFx.ring(level, HollowmancyParticles.RING_FROST.get(), player.position().add(0, 0.1, 0), (float) radius);
        SpellFx.runeCircle(level, HollowmancyParticles.RUNE_FROST.get(), player.position().add(0, 0.2, 0), radius * 0.6, 6);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.0f, 0.8f);
        return true;
    }
}