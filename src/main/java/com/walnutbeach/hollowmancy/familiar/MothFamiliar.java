package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Crystal Moth from the Hollow. It flutters just ahead of you and, in the dark, leaves a trail of soft light
 * behind you that fades after a while. Now and then it picks a flower nearby and brings it to you
 * (the "mothPicksFlowers" setting turns that off). At its strongest, its dust slows your falls.
 */
public class MothFamiliar extends FamiliarEntity {
    private record TrailLight(BlockPos pos, long until) {}
    private final List<TrailLight> trail = new ArrayList<>();
    private static final int MAX_LIGHTS = 32;

    private BlockPos flower = null;
    private int flowerTicks = 0;
    private int flowerCooldown = 200;

    public MothFamiliar(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return ModSounds.CRYSTAL_MOTH_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    public FamiliarType getFamiliarType() {
        return FamiliarType.MOTH;
    }

    private static int lightSeconds(int level) {
        return 20 + level * 10;   // 30 to 70 seconds
    }

    private static int lightLevel(int level) {
        return 8 + level;   // 9 to 13
    }

    private static int flowerSeconds(int level) {
        return 70 - level * 10;   // every 60 seconds, down to every 20
    }

    @Override
    public String describe(int level) {
        String text = FamiliarText.tr("hollowmancy.familiar.moth.describe", "leaves light behind you in the dark for %s seconds",
                lightSeconds(level));
        if (HollowmancyExtraConfig.get(HollowmancyExtraConfig.MOTH_PICKS_FLOWERS)) {
            text += FamiliarText.tr("hollowmancy.familiar.moth.describe.flowers", "; brings you a flower every %s seconds", flowerSeconds(level));
        }
        if (level >= 5) text += FamiliarText.tr("hollowmancy.familiar.moth.describe.dust", "; its dust slows your falls");
        return text;
    }

    @Override
    protected void blessOwner(ServerPlayer owner, int level) {
        if (level >= 5 && owner.fallDistance > 3.0F && !owner.onGround()) {
            owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false));
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 6 == 0) {
            server.sendParticles(HollowmancyParticles.FROST_SHARD.get(), getX(), getY() + 0.2, getZ(), 1, 0.15, 0.05, 0.15, 0.0);
        }
        tickTrail(server);
        ServerPlayer owner = ownerPlayer();
        if (owner == null) return;

        // Watching the owner fall: catch them a moment sooner than the 2-second blessing would
        if (getBondLevel() >= 5 && owner.fallDistance > 3.0F && tickCount % 5 == 0) blessOwner(owner, 5);

        if (isOrderedToSit()) {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            return;
        }
        if (tickFlower(server, owner)) return;

        // Flutter a little ahead of the owner, lighting the way
        Vec3 ahead = Vec3.directionFromRotation(0, owner.getYRot()).scale(1.6);
        double sway = Math.sin(tickCount * 0.13) * 0.5;
        Vec3 side = new Vec3(-ahead.z, 0, ahead.x).normalize().scale(sway);
        hoverToward(owner.position().add(ahead).add(side).add(0, 1.9 + Math.sin(tickCount * 0.21) * 0.2, 0));
        if (tickCount % 10 == 0) lightOwnerPath(server, owner);
    }

    // ---------------- The trail of light ----------------

    private boolean isDark(BlockPos pos) {
        int sky = level().getBrightness(LightLayer.SKY, pos) - level().getSkyDarken();
        return Math.max(sky, level().getBrightness(LightLayer.BLOCK, pos)) < 8;
    }

    private void lightOwnerPath(ServerLevel level, ServerPlayer owner) {
        BlockPos at = owner.blockPosition();
        if (!level.getBlockState(at).isAir() || !isDark(at)) return;
        for (TrailLight light : trail) if (light.pos().distSqr(at) < 9) return;   // one every 3 blocks is enough
        if (trail.size() >= MAX_LIGHTS) removeLight(trail.remove(0));
        level.setBlock(at, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel(getBondLevel())), 3);
        trail.add(new TrailLight(at, level.getGameTime() + lightSeconds(getBondLevel()) * 20L));
        level.sendParticles(HollowmancyParticles.FROST_SHARD.get(), at.getX() + 0.5, at.getY() + 0.3, at.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.01);
    }

    private void tickTrail(ServerLevel level) {
        if (trail.isEmpty() || tickCount % 20 != 0) return;
        long now = level.getGameTime();
        Iterator<TrailLight> it = trail.iterator();
        while (it.hasNext()) {
            TrailLight light = it.next();
            if (now < light.until()) continue;
            removeLight(light);
            it.remove();
        }
    }

    private void removeLight(TrailLight light) {
        if (level().getBlockState(light.pos()).is(Blocks.LIGHT)) level().removeBlock(light.pos(), false);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide) {
            trail.forEach(this::removeLight);
            trail.clear();
        }
        super.remove(reason);
    }

    // ---------------- Flowers ----------------

    /** Returns true while the moth is busy fetching a flower. */
    private boolean tickFlower(ServerLevel level, ServerPlayer owner) {
        if (!HollowmancyExtraConfig.get(HollowmancyExtraConfig.MOTH_PICKS_FLOWERS)) {
            flower = null;
            return false;
        }
        if (flower == null) {
            if (--flowerCooldown > 0) return false;
            flowerCooldown = flowerSeconds(getBondLevel()) * 20;
            flower = findFlower(level, owner);
            flowerTicks = 0;
            if (flower == null) return false;
        }
        BlockState state = level.getBlockState(flower);
        if (!state.is(BlockTags.SMALL_FLOWERS) || ++flowerTicks > 200 || distanceToSqr(owner) > 16 * 16) {
            flower = null;
            return false;
        }
        Vec3 target = Vec3.atBottomCenterOf(flower).add(0, 0.4, 0);
        hoverToward(target);
        getLookControl().setLookAt(target.x, target.y, target.z);
        if (position().distanceToSqr(target) < 0.6 * 0.6) {
            ItemStack picked = new ItemStack(state.getBlock().asItem());
            level.destroyBlock(flower, false);
            level.sendParticles(HollowmancyParticles.LEAF.get(), target.x, target.y, target.z, 6, 0.2, 0.2, 0.2, 0.02);
            if (!picked.isEmpty()) {
                if (!owner.getInventory().add(picked)) owner.drop(picked, false);
                level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 0.7F, 1.4F);
            }
            flower = null;
        }
        return true;
    }

    private static BlockPos findFlower(ServerLevel level, ServerPlayer owner) {
        BlockPos center = owner.blockPosition();
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 3, 6))) {
            if (level.getBlockState(pos).is(BlockTags.SMALL_FLOWERS)) found.add(pos.immutable());
        }
        return found.isEmpty() ? null : found.get(level.random.nextInt(found.size()));
    }
}
