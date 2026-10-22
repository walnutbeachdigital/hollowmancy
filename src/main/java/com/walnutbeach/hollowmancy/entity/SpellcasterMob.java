package com.walnutbeach.hollowmancy.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A hostile mob that fights by casting spells from range. */
public abstract class SpellcasterMob extends Monster implements RangedAttackMob {

    protected SpellcasterMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** Ticks between ranged casts (20 = 1 second). */
    protected abstract int castInterval();

    /** How close the caster tries to get before it starts casting. */
    protected abstract float castRange();

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        // Walks into range, stops, and calls performRangedAttack whenever it can see its target
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, castInterval(), castInterval() + 20, castRange()));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    /** Hollowmancy: tells the client when it's in a fight, so the model can hold its staff ready. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        setAggressive(getTarget() != null && getTarget().isAlive());
    }

    /** A line of particles from this caster's hands to a point. */
    protected void beam(ServerLevel level, ParticleOptions particle, Vec3 to) {
        Vec3 from = position().add(0, getBbHeight() * 0.7, 0);
        Vec3 path = to.subtract(from);
        double length = path.length();
        Vec3 dir = path.normalize();
        for (double d = 0.5; d < length; d += 0.4) {
            Vec3 point = from.add(dir.scale(d));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    /** A little burst at the hands, so players can see a cast coming. */
    protected void castFlash(ServerLevel level, ParticleOptions particle) {
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        level.sendParticles(particle, getX(), getY() + getBbHeight() * 0.7, getZ(), 10, 0.3, 0.2, 0.3, 0.05);
    }
}