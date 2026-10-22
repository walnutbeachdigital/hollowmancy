package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RogueApprentice extends SpellcasterMob {

    private int novaCooldown = 0;
    private int blinkCooldown = 0;

    public RogueApprentice(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected int castInterval() {
        return 50; // a Spark every 2.5 seconds
    }

    @Override
    protected float castRange() {
        return 14.0f;
    }

    // Spark: a bolt at its target
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel level)) return;
        castFlash(level, ParticleTypes.ELECTRIC_SPARK);
        beam(level, ParticleTypes.ELECTRIC_SPARK, target.getEyePosition());
        target.hurt(damageSources().indirectMagic(this, this), 3.0f);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0f, 1.6f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.4f, 1.8f);
    }

    // Up close: Frost Nova, or Blink away when badly hurt
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (novaCooldown > 0) novaCooldown--;
        if (blinkCooldown > 0) blinkCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || distanceToSqr(target) > 9.0) return;

        if (getHealth() < getMaxHealth() * 0.6f && blinkCooldown <= 0) {
            blinkAway(target);
            blinkCooldown = 200;
        } else if (novaCooldown <= 0) {
            frostNova();
            novaCooldown = 160;
        }
    }

    private void frostNova() {
        if (!(level() instanceof ServerLevel level)) return;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Monster))) {
            victim.hurt(damageSources().indirectMagic(this, this), 2.0f);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            for (double r = 1.0; r <= 4.0; r += 1.0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, getX() + Math.cos(rad) * r, getY() + 0.3, getZ() + Math.sin(rad) * r,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    private void blinkAway(LivingEntity target) {
        if (!(level() instanceof ServerLevel level)) return;
        Vec3 away = new Vec3(getX() - target.getX(), 0, getZ() - target.getZ());
        if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0);
        Vec3 destination = position().add(away.normalize().scale(7.0));

        Vec3 from = position();
        if (randomTeleport(destination.x, getY(), destination.z, true)) {
            level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 30, 0.3, 0.6, 0.3, 0.2);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.3, 0.6, 0.3, 0.05);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 1.4f);
        }
    }

    // Awakened players learn a little from what they carried
    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (alreadyDead || level().isClientSide) return;

        if (source.getEntity() instanceof ServerPlayer player && MagicHelper.hasStage(player, MagicStage.AWAKENING)) {
            MagicData data = MagicHelper.get(player);
            MagicHelper.set(player, data.withKnowledge(data.knowledge() + 1));
            player.displayClientMessage(Component.translatable("hollowmancy.rogue_apprentice.you_pocket_a_scrap_of_their")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
        }
    }
}