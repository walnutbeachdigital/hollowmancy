package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A tall shadow that drifts over the islands. It blinks closer when you keep your distance,
 * and its touch dims your sight. Takes the Enderman's place in the Hollow (and drops its pearls).
 */
public class HollowShade extends Monster {
    private int blinkCooldown = 60;

    public HollowShade(EntityType<? extends HollowShade> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (blinkCooldown > 0) blinkCooldown--;
        LivingEntity target = getTarget();
        if (target == null || blinkCooldown > 0) return;
        double distance = distanceTo(target);
        if (distance < 7.0 || distance > 28.0 || !hasLineOfSight(target)) return;

        // Blink to a spot a few blocks in front of the target
        Vec3 toMe = position().subtract(target.position()).normalize();
        Vec3 spot = target.position().add(toMe.scale(2.5 + random.nextDouble() * 1.5));
        Vec3 from = position();
        if (randomTeleport(spot.x, target.getY(), spot.z, false)) {
            ServerLevel level = (ServerLevel) level();
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), from.x, from.y + 1.2, from.z, 30, 0.3, 0.8, 0.3, 0.05);
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), getX(), getY() + 1.2, getZ(), 30, 0.3, 0.8, 0.3, 0.05);
            level.playSound(null, getX(), getY(), getZ(), ModSounds.HOLLOW_SHADE_BLINK.get(), SoundSource.HOSTILE, 0.8F, 1.0F);
            blinkCooldown = 90 + random.nextInt(60);
        } else {
            blinkCooldown = 20;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0), this);
        }
        return hit;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && random.nextInt(3) == 0) {
            level().addParticle(HollowmancyParticles.VOID_WISP.get(), getRandomX(0.5), getRandomY() - 0.25, getRandomZ(0.5), 0, 0, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HOLLOW_SHADE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HOLLOW_SHADE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HOLLOW_SHADE_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.55F + random.nextFloat() * 0.1F;
    }
}
