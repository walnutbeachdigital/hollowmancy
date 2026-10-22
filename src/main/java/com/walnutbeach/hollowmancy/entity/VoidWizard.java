package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
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

/** Keeper of a black tower in the shadowed places. It studied the Hollow, and the Hollow studied it back. */
public class VoidWizard extends TowerWizard {

    public VoidWizard(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override protected String title() { return "Void Wizard"; }
    @Override protected ChatFormatting voice() { return ChatFormatting.DARK_PURPLE; }
    @Override protected String[] greetings() {
        return new String[] {"...the Hollow sent you?", "Listen. Can you hear it too?", "There is so much room inside you."};
    }

    // Void Lance: heavy damage, and a little corruption
    @Override
    protected void rangedSpell(ServerLevel level, LivingEntity target) {
        castFlash(level, ParticleTypes.REVERSE_PORTAL);
        beam(level, ParticleTypes.SQUID_INK, target.getEyePosition());
        beam(level, ParticleTypes.REVERSE_PORTAL, target.getEyePosition());
        target.hurt(damageSources().indirectMagic(this, this), 6.0f);
        if (target instanceof ServerPlayer player) {
            MagicData data = MagicHelper.get(player);
            MagicHelper.set(player, data.withCorruption(data.corruption() + 1));
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.3f, 1.8f);
    }

    // Void Pulse: darkness, and a hard shove
    @Override
    protected void closeSpell(ServerLevel level) {
        for (LivingEntity victim : victimsNear(level, 4.5)) {
            victim.hurt(damageSources().indirectMagic(this, this), 4.0f);
            victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
            victim.knockback(1.6, getX() - victim.getX(), getZ() - victim.getZ());
        }
        ring(level, ParticleTypes.REVERSE_PORTAL, 4.5);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 0.8f, 0.6f);
    }

    // Rift: pulls the target to the wizard's side
    @Override
    protected void hexSpell(ServerLevel level, LivingEntity target) {
        Vec3 pull = position().subtract(target.position());
        Vec3 landing = target.position().add(pull.scale(0.8));
        level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0, target.getZ(), 40, 0.4, 0.8, 0.4, 0.3);
        if (target.randomTeleport(landing.x, landing.y, landing.z, true)) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.6f);
        }
    }
}
