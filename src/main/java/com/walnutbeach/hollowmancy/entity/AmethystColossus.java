package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AmethystColossus extends Monster {

    public static final ResourceKey<Biome> CRYSTAL_BARRENS =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "crystal_barrens"));
    public static final int MAX_MANA_REWARD = 100;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hollowmancy.amethyst_colossus"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_10);

    /** A spot where crystal will erupt, and when. */
    private record Eruption(Vec3 pos, long dueTick) {}
    private final List<Eruption> eruptions = new ArrayList<>();

    private int phase = 0;
    private int timer = 0;
    private int shieldTicks = 0;

    public AmethystColossus(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------------- Crystal: arrows glance off, magic cuts deep ----------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_FIRE)) {
            if (!level().isClientSide) playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.2f, 1.4f);
            return false;
        }
        if (source.is(DamageTypeTags.IS_LIGHTNING)) amount *= 2.0f;
        else if (source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.MAGIC)) amount *= 1.5f;
        if (shieldTicks > 0) amount *= 0.25f;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    // ---------------- The fight ----------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) return;
        bossEvent.setProgress(getHealth() / getMaxHealth());

        int newPhase = getHealth() > getMaxHealth() * 0.6f ? 1 : getHealth() > getMaxHealth() * 0.3f ? 2 : 3;
        if (phase == 0) {
            phase = newPhase;
        } else if (newPhase > phase) {
            phase = newPhase;
            speak(phase == 2 ? "The ground remembers the song." : "...LOUDER.");
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0f, 0.4f);
        }

        if (shieldTicks > 0) {
            shieldTicks--;
            if (shieldTicks % 5 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 6, 1.0, 1.5, 1.0, 0.02);
            }
        }
        tickEruptions(level);

        timer++;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        int lanceEvery = phase == 1 ? 90 : phase == 2 ? 70 : 50;
        if (timer % lanceEvery == 0) crystalLance(level, target);
        if (timer % 160 == 40 && distanceToSqr(target) < 64) resonancePulse(level);
        if (phase >= 2 && timer % 120 == 60) callEruptions(level);
        if (phase >= 3 && timer % 300 == 150) raiseShield(level);
    }

    private void crystalLance(ServerLevel level, LivingEntity target) {
        if (!hasLineOfSight(target)) return;
        Vec3 from = getEyePosition();
        Vec3 path = target.getEyePosition().subtract(from);
        Vec3 dir = path.normalize();
        for (double d = 0.5; d < path.length(); d += 0.4) {
            Vec3 p = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        target.hurt(damageSources().indirectMagic(this, this), phase >= 3 ? 10.0f : 8.0f);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    private void resonancePulse(ServerLevel level) {
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(7.0),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Monster))) {
            victim.hurt(damageSources().indirectMagic(this, this), 6.0f);
            victim.knockback(1.8, getX() - victim.getX(), getZ() - victim.getZ());
            victim.hurtMarked = true;
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
        for (int angle = 0; angle < 360; angle += 10) {
            double rad = Math.toRadians(angle);
            level.sendParticles(ParticleTypes.END_ROD, getX() + Math.cos(rad) * 4, getY() + 0.3, getZ() + Math.sin(rad) * 4,
                    1, 0, 0, 0, 0.02);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.5f, 0.5f);
    }

    // Phase 2+: mark the ground under every nearby player; it erupts a moment later
    private void callEruptions(ServerLevel level) {
        long due = level.getGameTime() + 25;
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(24),
                p -> p.isAlive() && !p.isSpectator())) {
            eruptions.add(new Eruption(player.position(), due));
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0f, 1.2f);
    }

    private void tickEruptions(ServerLevel level) {
        long now = level.getGameTime();
        Iterator<Eruption> it = eruptions.iterator();
        while (it.hasNext()) {
            Eruption eruption = it.next();
            Vec3 pos = eruption.pos();
            if (now < eruption.dueTick()) {
                if (now % 3 == 0) level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 0.1, pos.z, 3, 0.4, 0.02, 0.4, 0.01);
                continue;
            }
            it.remove();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),
                    pos.x, pos.y + 0.5, pos.z, 40, 0.4, 0.6, 0.4, 0.2);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.5f, 0.6f);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos, 3.0, 4.0, 3.0),
                    entity -> entity != this && entity.isAlive())) {
                victim.hurt(damageSources().mobAttack(this), 10.0f);
                victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.7, 0));
                victim.hurtMarked = true;
            }
        }
    }

    private void raiseShield(ServerLevel level) {
        shieldTicks = 100;
        speak("The song hardens.");
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    private void speak(String line) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(64))) {
            player.sendSystemMessage(Component.translatable("hollowmancy.amethyst_colossus.the_amethyst_colossus").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                    .append(Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }
    }

    // ---------------- Defeat and rewards ----------------

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (alreadyDead || !(level() instanceof ServerLevel level)) return;

        speak("...and then, silence.");
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                getX(), getY() + 2.0, getZ(), 150, 1.2, 2.0, 1.2, 0.3);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 3.0f, 0.5f);

        AdvancementHolder advancement = level.getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "colossus"));
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
            boolean firstTime = advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
            if (advancement != null) player.getAdvancements().award(advancement, "defeated");
            if (firstTime) {
                MagicData data = MagicHelper.get(player);
                MagicHelper.set(player, data.withMaxMana(data.maxMana() + MAX_MANA_REWARD));
                player.sendSystemMessage(Component.translatable("hollowmancy.amethyst_colossus.the_song_settles_in_you_max", MAX_MANA_REWARD)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
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
        return false;
    }
}