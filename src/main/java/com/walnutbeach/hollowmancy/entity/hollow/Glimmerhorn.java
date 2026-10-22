package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** A shy deer of the Hollow Grove with antlers of living crystal. Follows glow berries, and breeds with them. */
public class Glimmerhorn extends Animal {
    private int grazeTicks;
    private static final int GRAZE_LENGTH = 70;

    public Glimmerhorn(EntityType<? extends Glimmerhorn> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 0.24);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 2.1));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.1, this::isFood, false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return HollowCreatures.GLIMMERHORN.get().create(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Grazing is just for show, so the client decides when it happens
        if (level().isClientSide) {
            if (grazeTicks > 0) grazeTicks--;
            else if (getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && random.nextInt(500) == 0) grazeTicks = GRAZE_LENGTH;
        }
    }

    /** 0 = head up, 1 = head down in the grass. */
    public float getGrazeAmount(float partialTick) {
        if (grazeTicks <= 0) return 0.0F;
        float t = grazeTicks - partialTick;
        return Mth.clamp(Math.min(t, GRAZE_LENGTH - t) / 12.0F, 0.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GLIMMERHORN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLIMMERHORN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLIMMERHORN_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return (isBaby() ? 1.6F : 1.25F) + (random.nextFloat() - 0.5F) * 0.2F;
    }

    public static boolean checkGlimmerhornSpawnRules(EntityType<Glimmerhorn> type, ServerLevelAccessor level, MobSpawnType reason,
                                                     BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.getBlockState(pos).isAir();
    }
}
