package com.walnutbeach.hollowmancy.familiar;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/** Warding. Fights beside you, shields you while it's near, and brings you items lying around. */
public class GolemFamiliar extends FamiliarEntity {

    public GolemFamiliar(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public FamiliarType getFamiliarType() {
        return FamiliarType.GOLEM;
    }

    /** What it's carrying back to you. */
    private ItemStack carried = ItemStack.EMPTY;

    /** How far from you it looks for items (blocks). */
    private static int fetchRange(int level) {
        return 6 + level * 2;
    }

    private static double damage(int level) {
        return 3.0 + level * 1.5;
    }

    @Override
    public String describe(int level) {
        String text = FamiliarText.tr("hollowmancy.familiar.golem.describe", "fights for you (%s damage); fetches items within %s blocks", (int) damage(level), fetchRange(level));
        if (level >= 3) text += FamiliarText.tr("hollowmancy.familiar.golem.describe.resist", "; Resistance while it's near");
        if (level >= 5) text += FamiliarText.tr("hollowmancy.familiar.golem.describe.slow", "; its blows slow enemies");
        return text;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.3, true));
        goalSelector.addGoal(5, new BringItems());
        goalSelector.addGoal(6, new FetchItems());
        goalSelector.addGoal(7, new FollowOwner());
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    protected void onLevelChanged(int level) {
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage(level));
    }

    @Override
    protected void blessOwner(ServerPlayer owner, int level) {
        if (level >= 3 && distanceToSqr(owner) < 10 * 10) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && getBondLevel() >= 5 && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
        return hit;
    }

    /** Walk to an item lying nearby and pick it up. Items you threw yourself are left alone. */
    private class FetchItems extends Goal {
        private ItemEntity item;

        FetchItems() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ServerPlayer owner = ownerPlayer();
            if (owner == null || isOrderedToSit() || getTarget() != null || !carried.isEmpty() || tickCount % 10 != 0) return false;
            int range = fetchRange(getBondLevel());
            item = level().getEntitiesOfClass(ItemEntity.class, owner.getBoundingBox().inflate(range),
                            e -> e.isAlive() && !e.hasPickUpDelay() && e.getOwner() != owner)
                    .stream().min((a, b) -> Double.compare(a.distanceToSqr(GolemFamiliar.this), b.distanceToSqr(GolemFamiliar.this)))
                    .orElse(null);
            return item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return item != null && item.isAlive() && carried.isEmpty() && getTarget() == null && !isOrderedToSit() && ownerPlayer() != null;
        }

        @Override
        public void tick() {
            getLookControl().setLookAt(item, 10.0f, getMaxHeadXRot());
            if (tickCount % 10 == 0) getNavigation().moveTo(item, 1.15);
            if (distanceToSqr(item) < 2.0 * 2.0) {
                carried = item.getItem().copy();
                item.discard();
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.6f, 0.7f);
            }
        }

        @Override
        public void stop() {
            item = null;
            getNavigation().stop();
        }
    }

    /** Carry what it picked up back to the owner. */
    private class BringItems extends Goal {
        BringItems() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !carried.isEmpty() && ownerPlayer() != null && !isOrderedToSit();
        }

        @Override
        public void tick() {
            ServerPlayer owner = ownerPlayer();
            if (owner == null) return;
            getLookControl().setLookAt(owner, 10.0f, getMaxHeadXRot());
            if (tickCount % 10 == 0) getNavigation().moveTo(owner, 1.15);
            if (distanceToSqr(owner) < 2.5 * 2.5) {
                if (!owner.getInventory().add(carried)) owner.drop(carried, false);
                carried = ItemStack.EMPTY;
                level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.8f, 1.0f);
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }

    /** Anything it was carrying is dropped if it goes away. */
    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide && !carried.isEmpty()) {
            spawnAtLocation(carried);
            carried = ItemStack.EMPTY;
        }
        super.remove(reason);
    }

    /** Walk after the owner when there's nothing to fight. */
    private class FollowOwner extends Goal {
        FollowOwner() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ServerPlayer owner = ownerPlayer();
            return owner != null && !isOrderedToSit() && getTarget() == null && distanceToSqr(owner) > 4 * 4;
        }

        @Override
        public boolean canContinueToUse() {
            ServerPlayer owner = ownerPlayer();
            return owner != null && !isOrderedToSit() && getTarget() == null && distanceToSqr(owner) > 2.5 * 2.5;
        }

        @Override
        public void tick() {
            ServerPlayer owner = ownerPlayer();
            if (owner == null) return;
            getLookControl().setLookAt(owner, 10.0f, getMaxHeadXRot());
            if (tickCount % 10 == 0) getNavigation().moveTo(owner, 1.1);
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }
}

