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

/** Keeper of a red tower in the dry lands. Everything it touches burns. */
public class FireWizard extends TowerWizard {

    public FireWizard(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override protected String title() { return "Fire Wizard"; }
    @Override protected ChatFormatting voice() { return ChatFormatting.GOLD; }
    @Override protected String[] greetings() {
        return new String[] {"You smell of kindling.", "Come closer. I'm cold.", "Burn, then."};
    }

    // Firebolt every time: hits hard and sets you alight
    @Override
    protected void rangedSpell(ServerLevel level, LivingEntity target) {
        castFlash(level, ParticleTypes.FLAME);
        beam(level, ParticleTypes.FLAME, target.getEyePosition());
        beam(level, ParticleTypes.LAVA, target.getEyePosition());
        target.hurt(damageSources().indirectMagic(this, this), 5.0f);
        target.igniteForSeconds(5.0f);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0f, 0.9f);
    }

    // Flame Ring: everything close is set ablaze and thrown back
    @Override
    protected void closeSpell(ServerLevel level) {
        for (LivingEntity victim : victimsNear(level, 4.5)) {
            victim.hurt(damageSources().indirectMagic(this, this), 4.0f);
            victim.igniteForSeconds(6.0f);
            victim.knockback(1.0, getX() - victim.getX(), getZ() - victim.getZ());
        }
        ring(level, ParticleTypes.FLAME, 4.5);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.2f, 0.7f);
    }

    // Scorch: weakens and burns the target from a distance
    @Override
    protected void hexSpell(ServerLevel level, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        target.igniteForSeconds(8.0f);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.0f, 0.8f);
    }
}
