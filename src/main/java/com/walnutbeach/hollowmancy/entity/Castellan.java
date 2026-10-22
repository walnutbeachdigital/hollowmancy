package com.walnutbeach.hollowmancy.entity;

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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
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

/**
 * Sir Aldric, the Castellan: Morvane's keeper of keys, in the north-east tower.
 * Drags you in with his chain, bashes you back with his shield, and shrugs off arrows.
 */
public class Castellan extends Monster {

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.translatable("entity.hollowmancy.castellan"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    private BlockPos home;
    private int hookCooldown = 80, bashCooldown = 40;
    private boolean greeted = false, rallied = false;

    public Castellan(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 140.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0)          // + the iron axe
            .add(Attributes.MOVEMENT_SPEED, 0.27)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // The shield turns most of an arrow aside
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= 0.3f;
            if (!level().isClientSide) playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 1.0f);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) return;
        if (home == null) home = blockPosition();
        if (!hasRestriction()) restrictTo(home, 10);
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (hookCooldown > 0) hookCooldown--;
        if (bashCooldown > 0) bashCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        if (!greeted && target instanceof Player) {
            greeted = true;
            speak("Halt. None pass to the King's treasury.");
        }
        if (!rallied && getHealth() < getMaxHealth() * 0.5f) {
            rallied = true;
            speak("To arms! Defend the keys!");
            rally(level, target);
        }

        double dist = distanceTo(target);
        if (dist > 4.0 && dist < 14.0 && hookCooldown <= 0 && hasLineOfSight(target)) hook(level, target);
        else if (dist < 3.0 && bashCooldown <= 0) bash(level, target);
    }

    /** Chain hook: drag the target to his feet. */
    private void hook(ServerLevel level, LivingEntity target) {
        hookCooldown = 120;
        Vec3 from = position().add(0, 1.4, 0), to = target.getEyePosition();
        Vec3 path = to.subtract(from);
        for (double d = 0.5; d < path.length(); d += 0.5) {
            Vec3 p = from.add(path.normalize().scale(d));
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        Vec3 pull = position().subtract(target.position()).normalize().scale(1.3);
        target.setDeltaMovement(pull.x, 0.35, pull.z);
        target.hurtMarked = true;
        target.hurt(damageSources().mobAttack(this), 3.0f);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 1.2f, 0.6f);
    }

    /** Shield bash: hurl the target back, dazed. */
    private void bash(ServerLevel level, LivingEntity target) {
        bashCooldown = 100;
        swing(InteractionHand.OFF_HAND);
        target.hurt(damageSources().mobAttack(this), 5.0f);
        target.knockback(1.6, getX() - target.getX(), getZ() - target.getZ());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        target.hurtMarked = true;
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    /** Once, at half health: two Revenant Guard archers. */
    private void rally(ServerLevel level, LivingEntity target) {
        for (int i = 0; i < 2; i++) {
            RevenantGuard archer = CastleGuards.REVENANT_GUARD.get().create(level);
            if (archer == null) continue;
            archer.moveTo(getX() + (i == 0 ? 2 : -2), getY(), getZ(), random.nextFloat() * 360, 0);
            archer.armWithBow();
            archer.setTarget(target);
            level.addFreshEntity(archer);
            level.sendParticles(ParticleTypes.POOF, archer.getX(), archer.getY() + 1, archer.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void speak(String line) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(32))) {
            player.sendSystemMessage(Component.translatable("hollowmancy.castellan.sir_aldric").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append(Component.literal(line).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)));
        }
    }

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (!alreadyDead && !level().isClientSide) speak("The keys... forgive me, my king.");
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
