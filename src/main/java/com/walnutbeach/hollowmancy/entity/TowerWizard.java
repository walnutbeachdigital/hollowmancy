package com.walnutbeach.hollowmancy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Keeper of an Overworld tower. A real fight for an Apprentice; no match for a Master.
 * The Fire, Frost and Void wizards extend this and swap in their own spells.
 */
public class TowerWizard extends SpellcasterMob {

    private static final String[] GREETINGS = {
        "Another thief on my stairs.", "You should not have climbed so high.", "Out. OUT."
    };

    private boolean firebolt = false;
    private boolean greeted = false;
    private boolean warded = false;
    private int novaCooldown = 0;
    private int blinkCooldown = 0;
    private int hexCooldown = 120;

    public TowerWizard(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected int castInterval() {
        return 45;
    }

    @Override
    protected float castRange() {
        return 16.0f;
    }

    // ---- What each kind of wizard says and casts (override these) ----

    protected String title() {
        return "Tower Wizard";
    }

    protected ChatFormatting voice() {
        return ChatFormatting.BLUE;
    }

    protected String[] greetings() {
        return GREETINGS;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (level() instanceof ServerLevel level) rangedSpell(level, target);
    }

    /** At range. The arcane wizard alternates an Arcane Bolt and a Firebolt. */
    protected void rangedSpell(ServerLevel level, LivingEntity target) {
        firebolt = !firebolt;
        if (firebolt) {
            castFlash(level, ParticleTypes.FLAME);
            beam(level, ParticleTypes.FLAME, target.getEyePosition());
            target.hurt(damageSources().indirectMagic(this, this), 4.0f);
            target.igniteForSeconds(3.0f);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.8f, 1.2f);
        } else {
            castFlash(level, ParticleTypes.ENCHANT);
            beam(level, ParticleTypes.END_ROD, target.getEyePosition());
            target.hurt(damageSources().indirectMagic(this, this), 5.0f);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (novaCooldown > 0) novaCooldown--;
        if (blinkCooldown > 0) blinkCooldown--;
        if (hexCooldown > 0) hexCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || !(level() instanceof ServerLevel level)) return;

        if (!greeted && target instanceof Player) {
            greeted = true;
            speak(greetings()[random.nextInt(greetings().length)]);
        }

        // Once, when badly hurt: a ward
        if (!warded && getHealth() < getMaxHealth() * 0.5f) {
            warded = true;
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1));
            addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.0, getZ(), 60, 0.6, 1.0, 0.6, 0.6);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0f, 1.4f);
            speak("You will not have my tower!");
        }

        if (hexCooldown <= 0 && hasLineOfSight(target) && distanceToSqr(target) < 16 * 16) {
            hexCooldown = 300;
            hexSpell(level, target);
        }

        if (distanceToSqr(target) > 3.5 * 3.5) return;
        if (getHealth() < getMaxHealth() * 0.6f && blinkCooldown <= 0) {
            blinkAway(level, target);
            blinkCooldown = 160;
        } else if (novaCooldown <= 0) {
            closeSpell(level);
            novaCooldown = 120;
        }
    }

    /** Every 15 seconds. The arcane wizard lifts its target into the air (the fall is the danger). */
    protected void hexSpell(ServerLevel level, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
        level.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /** When someone gets close. The arcane wizard casts Frost Nova. */
    protected void closeSpell(ServerLevel level) {
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.5),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Monster))) {
            victim.hurt(damageSources().indirectMagic(this, this), 4.0f);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            victim.knockback(0.8, getX() - victim.getX(), getZ() - victim.getZ());
        }
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            for (double r = 1.0; r <= 4.5; r += 1.0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, getX() + Math.cos(rad) * r, getY() + 0.3, getZ() + Math.sin(rad) * r,
                    1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0f, 0.6f);
    }

    private void blinkAway(ServerLevel level, LivingEntity target) {
        Vec3 away = new Vec3(getX() - target.getX(), 0, getZ() - target.getZ());
        if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0);
        Vec3 destination = position().add(away.normalize().scale(5.0));
        Vec3 from = position();
        if (randomTeleport(destination.x, getY(), destination.z, true)) {
            level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 30, 0.3, 0.6, 0.3, 0.2);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 1.2f);
        }
    }

    protected void speak(String line) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(24))) {
            player.sendSystemMessage(Component.translatableWithFallback("hollowmancy.wizard_title." + title().toLowerCase(java.util.Locale.ROOT).replace(' ', '_'), title()).append(": ").withStyle(voice(), ChatFormatting.BOLD)
                .append(Component.translatableWithFallback("hollowmancy.wizard.say." + com.walnutbeach.hollowmancy.spell.Spell.slug(line), line).withStyle(voice(), ChatFormatting.ITALIC)));
        }
    }

    /** A burst of particles on the ground around the wizard, in rings out to `radius`. */
    protected void ring(ServerLevel level, net.minecraft.core.particles.ParticleOptions particle, double radius) {
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            for (double r = 1.0; r <= radius; r += 1.0) {
                level.sendParticles(particle, getX() + Math.cos(rad) * r, getY() + 0.3, getZ() + Math.sin(rad) * r, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    /** Everything near the wizard that isn't a monster. */
    protected java.util.List<LivingEntity> victimsNear(ServerLevel level, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
            entity -> entity != this && entity.isAlive() && !(entity instanceof Monster));
    }

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (!alreadyDead && !level().isClientSide) speak("...the tower... remembers...");
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;   // it lives here
    }
}
