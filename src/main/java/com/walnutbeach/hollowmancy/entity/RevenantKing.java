package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import com.walnutbeach.hollowmancy.entity.castle.CastleGuards;
import com.walnutbeach.hollowmancy.entity.castle.RevenantGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Morvane, the Revenant King. A towering armored knight who holds the throne room of his castle.
 * A melee fight: cleaves, leaps, raises his guard, and in the end sends shockwaves you must jump.
 */
public class RevenantKing extends Monster {

    public static final int KNOWLEDGE_REWARD = 10;
    private static final int HOME_RADIUS = 14;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.translatable("entity.hollowmancy.revenant_king"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private BlockPos home;
    private int phase = 0;
    private int cleaveCooldown = 60, leapCooldown = 100, summonCooldown = 100, waveCooldown = 60;
    private boolean leaping = false;
    private int leapTicks = 0;
    private double waveRadius = -1;
    private Vec3 waveCenter = Vec3.ZERO;
    private final Set<UUID> waveHit = new HashSet<>();
    private boolean greeted = false;

    public RevenantKing(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 800;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 360.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0)          // + the netherite sword
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 40.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
            .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------------- The fight ----------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) return;
        if (home == null) home = blockPosition();
        if (!hasRestriction()) restrictTo(home, HOME_RADIUS);   // he does not leave his throne room
        bossEvent.setProgress(getHealth() / getMaxHealth());

        int newPhase = getHealth() > getMaxHealth() * 0.66f ? 1 : getHealth() > getMaxHealth() * 0.33f ? 2 : 3;
        if (phase == 0) phase = newPhase;
        else if (newPhase > phase) {
            phase = newPhase;
            speak(phase == 2 ? "Guards! To your king!" : "I swore to hold this hall. I will hold it.");
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.5f);
            if (phase == 3) addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 600, 0));
        }

        if (cleaveCooldown > 0) cleaveCooldown--;
        if (leapCooldown > 0) leapCooldown--;
        if (summonCooldown > 0) summonCooldown--;
        if (waveCooldown > 0) waveCooldown--;
        tickLeap(level);
        tickWave(level);

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        if (!greeted && target instanceof Player) {
            greeted = true;
            speak("Kneel before Morvane, or be broken.");
        }

        double dist = distanceTo(target);
        if (dist < 4.0 && cleaveCooldown <= 0) cleave(level);
        else if (dist > 7.0 && dist < 22.0 && onGround() && !leaping && leapCooldown <= 0) leapAt(level, target);
        if (phase >= 2 && summonCooldown <= 0) summonGuards(level, target);
        if (phase >= 3 && waveCooldown <= 0 && waveRadius < 0) startWave(level);
    }

    /** A wide sweep of the blade: hurts and throws back everything around him. */
    private void cleave(ServerLevel level) {
        cleaveCooldown = phase >= 3 ? 70 : 100;
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.5),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Monster))) {
            victim.hurt(damageSources().mobAttack(this), 9.0f);
            victim.knockback(1.3, getX() - victim.getX(), getZ() - victim.getZ());
            victim.hurtMarked = true;
        }
        for (int angle = 0; angle < 360; angle += 30) {
            double rad = Math.toRadians(angle);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(rad) * 2.5, getY() + 1.2, getZ() + Math.sin(rad) * 2.5,
                1, 0, 0, 0, 0);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    /** Hurl himself at a distant target, and slam down where he lands. */
    private void leapAt(ServerLevel level, LivingEntity target) {
        leapCooldown = phase >= 3 ? 110 : 160;
        Vec3 toward = target.position().subtract(position());
        Vec3 flat = new Vec3(toward.x, 0, toward.z).normalize().scale(Math.min(1.4, toward.horizontalDistance() * 0.11));
        setDeltaMovement(flat.x, 0.75, flat.z);
        hasImpulse = true;
        leaping = true;
        leapTicks = 0;
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0f, 1.3f);
    }

    private void tickLeap(ServerLevel level) {
        if (!leaping) return;
        leapTicks++;
        if (leapTicks < 6 || !onGround()) {
            if (leapTicks > 60) leaping = false;
            return;
        }
        leaping = false;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Monster))) {
            victim.hurt(damageSources().mobAttack(this), 8.0f);
            victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.6, 0));
            victim.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 1.5, 0.1, 1.5, 0.0);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2f, 0.7f);
    }

    /** Phase 2+: two of his old guard climb out of the floor. */
    private void summonGuards(ServerLevel level, LivingEntity target) {
        summonCooldown = 400;
        int guards = level.getEntitiesOfClass(RevenantGuard.class, getBoundingBox().inflate(20)).size();
        if (guards >= 4) return;
        for (int i = 0; i < 2; i++) {
            RevenantGuard guard = CastleGuards.REVENANT_GUARD.get().create(level);
            if (guard == null) continue;
            double angle = random.nextDouble() * Math.PI * 2;
            guard.moveTo(getX() + Math.cos(angle) * 3, getY(), getZ() + Math.sin(angle) * 3, random.nextFloat() * 360, 0);
            guard.armWithSword();
            guard.setTarget(target);
            level.addFreshEntity(guard);
            level.sendParticles(ParticleTypes.SOUL, guard.getX(), guard.getY() + 0.5, guard.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    /** Phase 3: a ring of soul fire races out across the floor. Jump it. */
    private void startWave(ServerLevel level) {
        waveCooldown = 200;
        waveRadius = 0.5;
        waveCenter = position();
        waveHit.clear();
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    private void tickWave(ServerLevel level) {
        if (waveRadius < 0) return;
        waveRadius += 0.45;
        int points = (int) (waveRadius * 6) + 8;
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2 / points;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, waveCenter.x + Math.cos(a) * waveRadius, waveCenter.y + 0.15,
                waveCenter.z + Math.sin(a) * waveRadius, 1, 0, 0.02, 0, 0);
        }
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(waveRadius + 2))) {
            double d = Math.sqrt(player.distanceToSqr(waveCenter.x, player.getY(), waveCenter.z));
            boolean grounded = player.onGround() && player.getY() < waveCenter.y + 1.0;
            if (grounded && Math.abs(d - waveRadius) < 0.8 && waveHit.add(player.getUUID())) {
                player.hurt(damageSources().indirectMagic(this, this), 8.0f);
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
                player.hurtMarked = true;
                player.igniteForSeconds(2.0f);
            }
        }
        if (waveRadius > 16) waveRadius = -1;
    }

    private void speak(String line) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(48))) {
            player.sendSystemMessage(Component.translatable("hollowmancy.revenant_king.morvane").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                .append(Component.literal(line).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)));
        }
    }

    // ---------------- Defeat ----------------

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (alreadyDead || !(level() instanceof ServerLevel level)) return;
        speak("...the oath... is broken. Rest... at last.");
        level.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 120, 1.0, 1.5, 1.0, 0.08);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.0f, 1.2f);

        AdvancementHolder advancement = level.getServer().getAdvancements()
            .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "revenant_king"));
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(48))) {
            boolean firstTime = advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
            if (advancement != null) player.getAdvancements().award(advancement, "defeated");
            if (firstTime) {
                MagicData data = MagicHelper.get(player);
                MagicHelper.set(player, data.withKnowledge(data.knowledge() + KNOWLEDGE_REWARD));
                player.sendSystemMessage(Component.translatable("hollowmancy.revenant_king.morvane_s_memories_settle_in_you", KNOWLEDGE_REWARD)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
        }
    }

    // ---------------- Boss bar, saving, odds and ends ----------------

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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
