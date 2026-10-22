package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.entity.hollow.HollowCreatures;
import com.walnutbeach.hollowmancy.entity.hollow.MirrorLurker;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;

/**
 * The Leviathan of the Mirror Sea. A vast guardian of the deep that keeps the Abyssal Shrine.
 * Its song silences the sea (no Conduit Power near it, so watch your air), its laser burns,
 * its undertow drags you in, and when it's hurt the Mirror Lurkers rise to defend it.
 */
public class Leviathan extends Guardian {

    public static final int SONG_RADIUS = 40;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.translatable("entity.hollowmancy.leviathan"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_6);
    private final Set<UUID> warned = new HashSet<>();

    private BlockPos home;
    private boolean enraged = false;
    private int undertowCooldown = 100, slamCooldown = 60, summonCooldown = 40;

    public Leviathan(EntityType<? extends Guardian> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Guardian.createAttributes()
            .add(Attributes.MAX_HEALTH, 300.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.55)
            .add(Attributes.FOLLOW_RANGE, 40.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** How long its laser takes to charge: faster once it's hurt. */
    @Override
    public int getAttackDuration() {
        return enraged ? 40 : 60;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) return;
        if (home == null) home = blockPosition();
        if (!hasRestriction()) restrictTo(home, 24);
        // Never stuck in a wall: if it's wedged in blocks, it slips to the nearest open water
        if (unstickCooldown > 0) unstickCooldown--;
        if (tickCount % 10 == 0 && unstickCooldown <= 0 && !level.noCollision(this, getBoundingBox().deflate(0.05))) freeFromWalls(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());

        // Its song: no Conduit Power nearby, and a heavy weariness
        if (tickCount % 20 == 0) {
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(SONG_RADIUS))) {
                player.removeEffect(MobEffects.CONDUIT_POWER);
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 1, true, true));
                if (warned.add(player.getUUID())) {
                    player.sendSystemMessage(Component.translatable("hollowmancy.leviathan.a_song_rises_from_the_deep")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE,
                        SoundSource.HOSTILE, 1.0f, 0.6f);
                }
            }
        }

        if (!enraged && getHealth() < getMaxHealth() * 0.5f) {
            enraged = true;
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.HOSTILE, 3.0f, 0.4f);
        }
        if (undertowCooldown > 0) undertowCooldown--;
        if (slamCooldown > 0) slamCooldown--;
        if (summonCooldown > 0) summonCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        double dist = distanceTo(target);
        if (dist < 6.5 && slamCooldown <= 0) tidalSlam(level);
        else if (dist > 7 && dist < 24 && target.isInWater() && undertowCooldown <= 0) undertow(level, target);
        if (enraged && summonCooldown <= 0) summonLurkers(level, target);
    }

    private int unstickCooldown = 0;

    /** Blocks can't smother it: it moves out of them instead. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            if (level() instanceof ServerLevel level) freeFromWalls(level);
            return false;
        }
        return super.hurt(source, amount);
    }

    /** Moves to the nearest open water big enough for its body, within 16 blocks. */
    private void freeFromWalls(ServerLevel level) {
        if (unstickCooldown > 0) return;
        unstickCooldown = 40;
        BlockPos origin = blockPosition();
        for (int r = 1; r <= 16; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != r) continue;   // just this distance
                        BlockPos spot = origin.offset(dx, dy, dz);
                        if (spot.getY() <= level.getMinBuildHeight() + 1) continue;
                        Vec3 feet = Vec3.atBottomCenterOf(spot);
                        AABB box = getDimensions(getPose()).makeBoundingBox(feet);
                        if (!level.noCollision(this, box)) continue;
                        if (!level.getFluidState(BlockPos.containing(box.getCenter())).is(FluidTags.WATER)) continue;
                        teleportTo(feet.x, feet.y, feet.z);
                        home = spot;
                        restrictTo(home, 24);
                        return;
                    }
                }
            }
        }
    }

    /** Undertow: the water itself drags the target toward its jaws. */
    private void undertow(ServerLevel level, LivingEntity target) {
        undertowCooldown = enraged ? 110 : 160;
        Vec3 pull = position().subtract(target.position()).normalize().scale(1.1);
        target.setDeltaMovement(pull);
        target.hurtMarked = true;
        Vec3 from = target.position(), path = position().subtract(from);
        for (double d = 0; d < path.length(); d += 0.7) {
            Vec3 p = from.add(path.normalize().scale(d));
            level.sendParticles(ParticleTypes.BUBBLE, p.x, p.y + 0.5, p.z, 3, 0.2, 0.2, 0.2, 0.02);
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
            SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    /** Tidal Slam: a crushing wave all around it. */
    private void tidalSlam(ServerLevel level) {
        slamCooldown = enraged ? 80 : 120;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Guardian) && !(entity instanceof MirrorLurker))) {
            victim.hurt(damageSources().mobAttack(this), 8.0f);
            victim.knockback(2.0, getX() - victim.getX(), getZ() - victim.getZ());
            victim.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 1.5, getZ(), 120, 3.0, 1.5, 3.0, 0.2);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 3.0f, 0.4f);
    }

    /** Once hurt: the Mirror Lurkers of the sea floor rise to defend it. (Vanilla Drowned are banned from the Hollow.) */
    private void summonLurkers(ServerLevel level, LivingEntity target) {
        summonCooldown = 400;
        if (level.getEntitiesOfClass(MirrorLurker.class, getBoundingBox().inflate(24)).size() >= 4) return;
        for (int i = 0; i < 2; i++) {
            MirrorLurker lurker = HollowCreatures.MIRROR_LURKER.get().create(level);
            if (lurker == null) continue;
            lurker.moveTo(getX() + random.nextInt(7) - 3, getY(), getZ() + random.nextInt(7) - 3, random.nextFloat() * 360, 0);
            lurker.setTarget(target);
            level.addFreshEntity(lurker);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, lurker.getX(), lurker.getY(), lurker.getZ(), 30, 0.3, 1.0, 0.3, 0.1);
        }
    }

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (alreadyDead || !(level() instanceof ServerLevel level)) return;
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(SONG_RADIUS))) {
            player.sendSystemMessage(Component.translatable("hollowmancy.leviathan.the_song_ends_the_sea_breathes")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (home != null) tag.put("home", NbtUtils.writeBlockPos(home));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.readBlockPos(tag, "home").ifPresent(pos -> home = pos);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
