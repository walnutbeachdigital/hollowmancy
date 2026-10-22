package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** A drifting mote of light. Harmless and curious. Cyan in the Hollow, violet in the Barrens, gold in the Grove. */
public class Wisp extends HollowFlyer {
    public static final String[] VARIANTS = {"cyan", "violet", "gold"};
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.INT);

    public Wisp(EntityType<? extends Wisp> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override protected double cruiseSpeed() { return 0.09; }
    @Override protected double wanderRange() { return 7.0; }
    @Override protected float curiosity() { return 0.35F; }
    @Override protected int minHeight() { return 1; }
    @Override protected int maxHeight() { return 6; }
    @Override protected double turning() { return 0.04; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public String getVariant() {
        return VARIANTS[Math.floorMod(entityData.get(VARIANT), VARIANTS.length)];
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        String biome = level.getBiome(blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("");
        entityData.set(VARIANT, biome.contains("barrens") ? 1 : biome.contains("grove") ? 2 : 0);
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", entityData.get(VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VARIANT, tag.getInt("Variant"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && random.nextInt(4) == 0) {
            level().addParticle(HollowmancyParticles.ARCANE_SPARK.get(), getRandomX(0.3), getY() + 0.25 + (random.nextDouble() - 0.5) * 0.3,
                    getRandomZ(0.3), 0, -0.01, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WISP_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WISP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISP_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.35F;
    }

    @Override
    public float getVoicePitch() {
        return 1.4F + random.nextFloat() * 0.4F;
    }
}
