package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import com.walnutbeach.hollowmancy.entity.hollow.HollowSummons;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HollowSovereign extends Monster {

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hollowmancy.hollow_sovereign"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private int phase = 0;        // 0 = not yet set; 1..3 during the fight
    private int abilityTimer = 0;

    public HollowSovereign(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.bossEvent.setDarkenScreen(true);
        this.bossEvent.setCreateWorldFog(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------------- The fight ----------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        int newPhase = getHealth() > getMaxHealth() * 0.66f ? 1 : getHealth() > getMaxHealth() * 0.33f ? 2 : 3;
        if (phase == 0) {
            phase = newPhase; // first tick (or loaded from a save): no announcement
        } else if (newPhase > phase) {
            phase = newPhase;
            announcePhase();
        }

        abilityTimer++;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        int lanceEvery = phase == 1 ? 100 : phase == 2 ? 70 : 45;
        if (abilityTimer % lanceEvery == 0) castVoidLance(target);
        if (phase >= 2 && abilityTimer % 300 == 0) summonShades(target);
        if (phase >= 3 && abilityTimer % 160 == 80) blinkBehind(target);
        if (phase >= 3 && abilityTimer % 200 == 0) darknessPulse();
    }

    private void castVoidLance(LivingEntity target) {
        if (!hasLineOfSight(target)) return;
        ServerLevel level = (ServerLevel) level();

        Vec3 from = getEyePosition();
        Vec3 path = target.getEyePosition().subtract(from);
        Vec3 dir = path.normalize();
        for (double d = 0.5; d < path.length(); d += 0.4) {
            Vec3 point = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.SQUID_INK, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0);
        }

        target.hurt(damageSources().indirectMagic(this, this), phase >= 3 ? 10.0f : 7.0f);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    private void summonShades(LivingEntity target) {
        ServerLevel level = (ServerLevel) level();
        // In the Hollow these are Hollow Shades (vanilla Vexes aren't allowed there); elsewhere, Vexes
        if (HollowSummons.count(level, this, 32) >= 6) return;

        for (int i = 0; i < 2; i++) {
            var servant = HollowSummons.summon(level, this, target,
                    getX() + random.nextGaussian() * 1.5, getY() + 2.0, getZ() + random.nextGaussian() * 1.5, 20 * 30);
            if (servant == null) continue;
            level.sendParticles(ParticleTypes.SCULK_SOUL, servant.getX(), servant.getY(), servant.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    private void blinkBehind(LivingEntity target) {
        ServerLevel level = (ServerLevel) level();
        Vec3 behind = target.position().subtract(target.getLookAngle().multiply(2.5, 0, 2.5));
        AABB box = getBoundingBox().move(behind.subtract(position()));
        if (!level.noCollision(this, box)) return;

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5, getZ(), 50, 0.5, 1.0, 0.5, 0.1);
        teleportTo(behind.x, behind.y, behind.z);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5, getZ(), 50, 0.5, 1.0, 0.5, 0.1);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    private void darknessPulse() {
        ServerLevel level = (ServerLevel) level();
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(32))) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 3.0f, 0.5f);
    }

    private void announcePhase() {
        String line = phase == 2
                ? "You carry their corruption too. I can taste it."
                : "Seven of us. SEVEN. And you think you are different?";
        speak(line);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.5f);
    }

    private void speak(String line) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(64))) {
            player.sendSystemMessage(Component.translatable("hollowmancy.hollow_sovereign.the_hollow_sovereign").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                    .append(Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }
    }

    // His touch corrupts
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer player) {
            MagicData data = MagicHelper.get(player);
            MagicHelper.set(player, data.withCorruption(data.corruption() + 3));
        }
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel level)) return;

        speak("...thank you. No one should hold it alone.");
        level.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.5, getZ(), 120, 1.0, 1.5, 1.0, 0.1);

        AdvancementHolder advancement = level.getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "sovereign"));
        if (advancement == null) return;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
            player.getAdvancements().award(advancement, "defeated");
        }
    }

    // ---------------- Boss bar & persistence ----------------

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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // never despawns
    }
}