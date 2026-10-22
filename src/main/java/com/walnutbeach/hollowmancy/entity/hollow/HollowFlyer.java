package com.walnutbeach.hollowmancy.entity.hollow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Small harmless things that drift through the air (wisps, crystal moths). They wander toward
 * random points near the ground, sometimes come over to look at a player, and dart away when hit.
 */
public abstract class HollowFlyer extends AmbientCreature {
    private Vec3 target;
    private int fleeTicks;

    protected HollowFlyer(EntityType<? extends HollowFlyer> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    // ---- How each kind moves (override to taste) ----
    /** Top speed, blocks per tick. */
    protected abstract double cruiseSpeed();
    /** How far it wanders from where it is. */
    protected abstract double wanderRange();
    /** 0..1: how often it comes to look at a nearby player. */
    protected abstract float curiosity();
    /** Lowest and highest height above the ground it likes to fly at. */
    protected abstract int minHeight();
    protected abstract int maxHeight();
    /** How sharply it turns: low = graceful drifting, high = jittery fluttering. */
    protected abstract double turning();

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(1.0, 0.85, 1.0));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (fleeTicks > 0) fleeTicks--;
        if (target == null || random.nextInt(60) == 0 || distanceToSqr(target) < 1.0) pickTarget();

        Vec3 to = target.subtract(position());
        double length = to.length();
        if (length > 0.01) {
            double speed = cruiseSpeed() * (fleeTicks > 0 ? 2.2 : 1.0);
            Vec3 wanted = to.scale(speed / length);
            Vec3 now = getDeltaMovement();
            setDeltaMovement(now.add(wanted.subtract(now).scale(turning())));
        }
        Vec3 move = getDeltaMovement();
        if (move.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(move.z, move.x) * Mth.RAD_TO_DEG) - 90.0F;
            setYRot(Mth.rotLerp(0.3F, getYRot(), yaw));
            yBodyRot = getYRot();
            yHeadRot = getYRot();
        }
    }

    private void pickTarget() {
        Player player = level().getNearestPlayer(this, 12.0);
        if (player != null && fleeTicks == 0 && random.nextFloat() < curiosity()) {
            // Come close, but not too close
            Vec3 side = new Vec3(random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5).normalize().scale(2.0 + random.nextDouble() * 2.0);
            target = player.getEyePosition().add(side).add(0, random.nextDouble() - 0.3, 0);
            return;
        }
        double r = wanderRange();
        double x = getX() + (random.nextDouble() - 0.5) * 2 * r;
        double z = getZ() + (random.nextDouble() - 0.5) * 2 * r;
        int ground = groundBelow(BlockPos.containing(x, getY(), z));
        double y = ground == Integer.MIN_VALUE ? getY() - 1
                : ground + minHeight() + random.nextDouble() * (maxHeight() - minHeight());
        target = new Vec3(x, y, z);
    }

    /** The y of the first solid block within 12 below, or MIN_VALUE if there's only air (the void). */
    private int groundBelow(BlockPos from) {
        BlockPos.MutableBlockPos pos = from.mutable();
        for (int i = 0; i < 12; i++) {
            if (!level().getBlockState(pos).isAir()) return pos.getY() + 1;
            pos.move(0, -1, 0);
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (hurt && attacker != null) {
            Vec3 away = position().subtract(attacker.position()).normalize().scale(8.0);
            target = position().add(away).add(0, 2, 0);
            fleeTicks = 60;
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    /** Spawns in open air with ground somewhere below it (never over the void). */
    public static boolean checkFlyerSpawnRules(EntityType<? extends HollowFlyer> type, ServerLevelAccessor level, MobSpawnType reason,
                                               BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir()) return false;
        return hasGroundBelow(level, pos);
    }

    static boolean hasGroundBelow(LevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 0; i < 10; i++) {
            p.move(0, -1, 0);
            if (!level.getBlockState(p).isAir()) return true;
        }
        return false;
    }
}
