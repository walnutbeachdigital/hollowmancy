package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A Glimmerhorn from the Hollow that lets you ride it. Right-click to climb on (sneak + right-click releases it,
 * like any familiar). It steps up whole blocks without jumping, gets faster with its bond, and at level 4
 * its horns light the way: Night Vision while you ride.
 */
public class GlimmerhornFamiliar extends FamiliarEntity {

    public GlimmerhornFamiliar(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return ModSounds.GLIMMERHORN_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 320;
    }

    @Override
    public FamiliarType getFamiliarType() {
        return FamiliarType.GLIMMERHORN;
    }

    /** A sprinting player's speed, in the same units. */
    private static final float SPRINT_SPEED = 0.13F;

    /** Riding speed bonus over sprinting, in percent. */
    private static int speedBonus(int level) {
        return 20 + level * 15;   // 35% to 95%
    }

    private static double stepHeight(int level) {
        return level >= 3 ? 1.6 : 1.1;
    }

    @Override
    public String describe(int level) {
        String text = FamiliarText.tr("hollowmancy.familiar.glimmerhorn.describe", "right-click to ride; %s%% faster than sprinting; steps up %s blocks",
                speedBonus(level), level >= 3 ? "1.5" : "1");
        if (level >= 4) text += FamiliarText.tr("hollowmancy.familiar.glimmerhorn.describe.light", "; its horns light the way while you ride");
        return text;
    }

    @Override
    protected void onLevelChanged(int level) {
        AttributeInstance step = getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) step.setBaseValue(stepHeight(level));
    }

    @Override
    protected void blessOwner(ServerPlayer owner, int level) {
        if (level >= 4 && owner.getVehicle() == this) {
            owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 15, 0, false, false));
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(5, new FollowOwner());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level() instanceof ServerLevel server && tickCount % 8 == 0) {
            double yaw = Math.toRadians(getYRot());
            double hx = getX() - Math.sin(yaw) * 0.9, hz = getZ() + Math.cos(yaw) * 0.9;
            server.sendParticles(HollowmancyParticles.FROST_SHARD.get(), hx, getY() + 1.7, hz, 1, 0.15, 0.1, 0.15, 0.0);
        }
    }

    // ---------------- Riding ----------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isOwnedBy(player) || player.isShiftKeyDown() || isVehicle()) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            setOrderedToSit(false);
            getNavigation().stop();
            serverPlayer.startRiding(this);
            serverPlayer.displayClientMessage(Component.literal(statusLine()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 0.6F, 1.3F);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player && isOwnedBy(player) ? player : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        setYRot(player.getYRot());
        yRotO = getYRot();
        setXRot(player.getXRot() * 0.5F);
        setRot(getYRot(), getXRot());
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float strafe = player.xxa * 0.5F;
        float forward = player.zza;
        if (forward <= 0.0F) forward *= 0.25F;
        return new Vec3(strafe, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return SPRINT_SPEED * (1.0F + speedBonus(getBondLevel()) / 100.0F);
    }

    /** Walk after the owner when no one is riding. */
    private class FollowOwner extends Goal {
        FollowOwner() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ServerPlayer owner = ownerPlayer();
            return owner != null && !isVehicle() && !isOrderedToSit() && distanceToSqr(owner) > 5 * 5;
        }

        @Override
        public boolean canContinueToUse() {
            ServerPlayer owner = ownerPlayer();
            return owner != null && !isVehicle() && !isOrderedToSit() && distanceToSqr(owner) > 3 * 3;
        }

        @Override
        public void tick() {
            ServerPlayer owner = ownerPlayer();
            if (owner == null) return;
            getLookControl().setLookAt(owner, 10.0f, getMaxHeadXRot());
            if (tickCount % 10 == 0) getNavigation().moveTo(owner, 1.2);
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }
}
