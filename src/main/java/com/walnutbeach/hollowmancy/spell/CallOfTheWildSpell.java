package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;

import java.util.Comparator;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class CallOfTheWildSpell extends Spell {

    public CallOfTheWildSpell() {
        super("call_of_the_wild", "Call of the Wild", "Two spirit wolves answer your call and fight beside you for 30 seconds.",
                MagicSchool.NATURE, MagicStage.MASTERY, 7, 70, 1200);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Three wolves, for 40 seconds."
                : "Four wolves, for 50 seconds, and they strike with Strength.";
    }

    @Override
    public int getChargeTicks() {
        return 20; // 1 second
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        int count = tier + 1;
        int lifeTicks = tier == 1 ? 600 : tier == 2 ? 800 : 1000;

        ServerLevel level = player.serverLevel();

        // Give the pack something to hunt straight away
        LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16),
                        entity -> entity instanceof Enemy && entity.isAlive())
                .stream().min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player))).orElse(null);

        for (int i = 0; i < count; i++) {
            Wolf wolf = EntityType.WOLF.create(level);
            if (wolf == null) continue;

            double angle = (Math.PI * 2 / count) * i;
            wolf.moveTo(player.getX() + Math.cos(angle) * 1.5, player.getY(), player.getZ() + Math.sin(angle) * 1.5,
                    player.getYRot(), 0.0f);
            wolf.tame(player);
            wolf.setHealth(wolf.getMaxHealth());
            if (tier >= 3) wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, lifeTicks, 0));
            if (prey != null) wolf.setTarget(prey);
            SpiritWolves.mark(wolf, level.getGameTime() + lifeTicks);

            level.addFreshEntity(wolf);
            level.sendParticles(HollowmancyParticles.LEAF.get(), wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                    10, 0.3, 0.3, 0.3, 0.0);
            level.sendParticles(HollowmancyParticles.RUNE_NATURE.get(), wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                    6, 0.2, 0.2, 0.2, 0.02);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.2f, 1.0f);
        return true;
    }
}