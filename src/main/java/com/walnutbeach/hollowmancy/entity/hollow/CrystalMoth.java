package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** A moth with wings of thin crystal. Flutters low and jittery, and likes to circle players. */
public class CrystalMoth extends HollowFlyer {
    public CrystalMoth(EntityType<? extends CrystalMoth> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override protected double cruiseSpeed() { return 0.13; }
    @Override protected double wanderRange() { return 4.0; }
    @Override protected float curiosity() { return 0.5F; }
    @Override protected int minHeight() { return 1; }
    @Override protected int maxHeight() { return 3; }
    @Override protected double turning() { return 0.18; }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTAL_MOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTAL_MOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTAL_MOTH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }

    @Override
    public float getVoicePitch() {
        return 1.6F + random.nextFloat() * 0.3F;
    }
}
