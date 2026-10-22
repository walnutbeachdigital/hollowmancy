package com.walnutbeach.hollowmancy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Keeper of a white tower in the snow. It wants you to stop moving. */
public class FrostWizard extends TowerWizard {

    public FrostWizard(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override protected String title() { return "Frost Wizard"; }
    @Override protected ChatFormatting voice() { return ChatFormatting.AQUA; }
    @Override protected String[] greetings() {
        return new String[] {"Be still.", "The cold keeps what it takes.", "You'll stay here a long while."};
    }

    // Ice Shard: slows, and frosts the edges of your vision
    @Override
    protected void rangedSpell(ServerLevel level, LivingEntity target) {
        castFlash(level, ParticleTypes.SNOWFLAKE);
        beam(level, ParticleTypes.SNOWFLAKE, target.getEyePosition());
        target.hurt(damageSources().freeze(), 5.0f);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 80));
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.8f, 1.6f);
    }

    // Deep Frost Nova: a stronger nova that nearly stops you
    @Override
    protected void closeSpell(ServerLevel level) {
        for (LivingEntity victim : victimsNear(level, 5.0)) {
            victim.hurt(damageSources().freeze(), 5.0f);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
            victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 60);
        }
        ring(level, ParticleTypes.SNOWFLAKE, 5.0);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.POWDER_SNOW_BREAK, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    // Frostbind: rooted in place for two seconds
    @Override
    protected void hexSpell(ServerLevel level, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 6));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1));
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 0.5, target.getZ(), 40, 0.4, 0.5, 0.4, 0.02);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 1.0f, 0.8f);
    }
}
