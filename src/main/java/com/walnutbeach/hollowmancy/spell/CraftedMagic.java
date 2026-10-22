package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.spell.CraftedSpell.Element;
import com.walnutbeach.hollowmancy.spell.CraftedSpell.Modifier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

/** Runs crafted spells: flying bolts, novas, beams, and the lingering zones they leave behind. */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class CraftedMagic {

    private static final double BOLT_SPEED = 1.6;
    private static final int BOLT_LIFE = 40;
    private static final double NOVA_RADIUS = 5.0;
    private static final double BEAM_RANGE = 20.0;

    private static final class Bolt {
        final ServerLevel level; final UUID owner; final CraftedSpell spell; final float scale;
        Vec3 pos, velocity; int age = 0;
        Bolt(ServerLevel level, UUID owner, CraftedSpell spell, Vec3 pos, Vec3 velocity, float scale) {
            this.level = level; this.owner = owner; this.spell = spell; this.pos = pos; this.velocity = velocity; this.scale = scale;
        }
    }

    private static final class Zone {
        final ServerLevel level; final UUID owner; final Element element; final Vec3 center; final double radius; final float damage;
        int ticksLeft;
        Zone(ServerLevel level, UUID owner, Element element, Vec3 center, double radius, float damage, int ticks) {
            this.level = level; this.owner = owner; this.element = element; this.center = center; this.radius = radius;
            this.damage = damage; this.ticksLeft = ticks;
        }
    }

    private static final class Delayed {
        final ServerLevel level; int ticks; final Runnable action;
        Delayed(ServerLevel level, int ticks, Runnable action) { this.level = level; this.ticks = ticks; this.action = action; }
    }

    private static final List<Bolt> BOLTS = new ArrayList<>();
    private static final List<Zone> ZONES = new ArrayList<>();
    private static final List<Delayed> DELAYED = new ArrayList<>();

    // ---------------- Casting ----------------

    public static void cast(ServerPlayer player, CraftedSpell spell) {
        switch (spell.shape) {
            case BOLT -> castBolt(player, spell);
            case NOVA -> castNova(player, spell);
            case BEAM -> castBeam(player, spell);
        }
    }

    private static void castBolt(ServerPlayer player, CraftedSpell spell) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
        boolean split = spell.modifier == Modifier.SPLIT;
        for (int i = split ? -1 : 0; i <= (split ? 1 : 0); i++) {
            Vec3 dir = look.yRot((float) Math.toRadians(-12 * i));
            BOLTS.add(new Bolt(level, player.getUUID(), spell, eye.add(dir.scale(0.6)), dir.scale(BOLT_SPEED), split ? 0.6f : 1.0f));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), castSound(spell.element), SoundSource.PLAYERS, 0.8f, 1.3f);
    }

    private static void castNova(ServerPlayer player, CraftedSpell spell) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position();
        if (spell.modifier == Modifier.HOMING) {
            LivingEntity target = nearestEnemy(level, player, player.position(), 16, null);
            if (target != null) center = target.position();
        }
        Vec3 at = center;
        nova(level, player, spell.element, at, spell.shape.damage);
        if (spell.modifier == Modifier.SPLIT) {
            DELAYED.add(new Delayed(level, 12, () -> withOwner(level, player.getUUID(),
                owner -> nova(level, owner, spell.element, at, spell.shape.damage * 0.6f))));
        }
        if (spell.modifier == Modifier.LINGERING) {
            ZONES.add(new Zone(level, player.getUUID(), spell.element, at, 4.0, spell.element.damage * 0.3f, 100));
        }
    }

    private static void castBeam(ServerPlayer player, CraftedSpell spell) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
        if (spell.modifier == Modifier.HOMING) {
            LivingEntity target = nearestEnemy(level, player, eye, BEAM_RANGE, look);
            if (target != null) look = target.getBoundingBox().getCenter().subtract(eye).normalize();
        }
        boolean split = spell.modifier == Modifier.SPLIT;
        for (int i = split ? -1 : 0; i <= (split ? 1 : 0); i++) {
            Vec3 dir = look.yRot((float) Math.toRadians(-12 * i));
            Vec3 end = beam(level, player, spell.element, eye, dir, spell.shape.damage * (i == 0 ? 1.0f : 0.6f));
            if (spell.modifier == Modifier.LINGERING) {
                Vec3 path = end.subtract(eye);
                for (double d = 3; d < path.length(); d += 3) {
                    Vec3 p = eye.add(path.normalize().scale(d));
                    ZONES.add(new Zone(level, player.getUUID(), spell.element, new Vec3(p.x, p.y - 1.2, p.z), 1.6,
                        spell.element.damage * 0.25f, 80));
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), castSound(spell.element), SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    // ---------------- The effects ----------------

    private static void nova(ServerLevel level, ServerPlayer owner, Element element, Vec3 center, float scale) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(center, NOVA_RADIUS * 2, 4, NOVA_RADIUS * 2),
                e -> canHarm(e, owner) && e.position().distanceTo(center) <= NOVA_RADIUS)) {
            strike(owner, target, element, element.damage * scale);
        }
        for (int angle = 0; angle < 360; angle += 10) {
            double rad = Math.toRadians(angle);
            for (double r = 1.5; r <= NOVA_RADIUS; r += 1.2) {
                level.sendParticles(particle(element), center.x + Math.cos(rad) * r, center.y + 0.3, center.z + Math.sin(rad) * r,
                    1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        level.playSound(null, center.x, center.y, center.z, impactSound(element), SoundSource.PLAYERS, 1.2f, 0.8f);
    }

    /** A piercing line; returns where it stopped. */
    private static Vec3 beam(ServerLevel level, ServerPlayer owner, Element element, Vec3 from, Vec3 dir, float scale) {
        Vec3 to = from.add(dir.scale(BEAM_RANGE));
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (block.getType() != HitResult.Type.MISS) to = block.getLocation();
        AABB box = new AABB(from, to).inflate(1.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> canHarm(e, owner))) {
            Vec3 a = from, b = to;
            Optional<Vec3> crossing = target.getBoundingBox().inflate(0.3).clip(a, b);
            if (crossing.isPresent()) strike(owner, target, element, element.damage * scale);
        }
        Vec3 path = to.subtract(from);
        for (double d = 0.8; d < path.length(); d += 0.35) {
            Vec3 p = from.add(path.normalize().scale(d));
            level.sendParticles(particle(element), p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        return to;
    }

    private static void strike(ServerPlayer owner, LivingEntity target, Element element, float damage) {
        target.invulnerableTime = 0;   // so split bolts and beams all land
        target.hurt(owner.damageSources().indirectMagic(owner, owner), damage);
        switch (element) {
            case FIRE -> target.igniteForSeconds(4.0f);
            case FROST -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + 80));
            }
            case VOID -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        }
        ((ServerLevel) target.level()).sendParticles(impactParticle(element), target.getX(), target.getY() + target.getBbHeight() / 2,
            target.getZ(), 10, 0.25, 0.3, 0.25, 0.04);
    }

    // ---------------- Every tick ----------------

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        tickBolts(level);
        tickZones(level);
        Iterator<Delayed> delayed = DELAYED.iterator();
        while (delayed.hasNext()) {
            Delayed task = delayed.next();
            if (task.level != level) continue;
            if (--task.ticks <= 0) {
                delayed.remove();
                task.action.run();
            }
        }
    }

    private static void tickBolts(ServerLevel level) {
        Iterator<Bolt> it = BOLTS.iterator();
        while (it.hasNext()) {
            Bolt bolt = it.next();
            if (bolt.level != level) continue;
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(bolt.owner);
            if (owner == null || ++bolt.age > BOLT_LIFE) {
                it.remove();
                continue;
            }
            if (bolt.spell.modifier == Modifier.HOMING && bolt.age > 2) {
                LivingEntity target = nearestEnemy(level, owner, bolt.pos, 16, null);
                if (target != null) {
                    Vec3 want = target.getBoundingBox().getCenter().subtract(bolt.pos).normalize();
                    bolt.velocity = bolt.velocity.normalize().scale(0.75).add(want.scale(0.25)).normalize().scale(BOLT_SPEED);
                }
            }
            Vec3 next = bolt.pos.add(bolt.velocity);
            BlockHitResult block = level.clip(new ClipContext(bolt.pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            Vec3 stop = block.getType() == HitResult.Type.MISS ? next : block.getLocation();

            LivingEntity struck = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(bolt.pos, stop).inflate(1.0),
                    e -> canHarm(e, owner))) {
                Optional<Vec3> crossing = target.getBoundingBox().inflate(0.3).clip(bolt.pos, stop);
                if (crossing.isPresent() && crossing.get().distanceToSqr(bolt.pos) < best) {
                    best = crossing.get().distanceToSqr(bolt.pos);
                    struck = target;
                }
            }
            for (double d = 0; d < 1.0; d += 0.25) {
                Vec3 p = bolt.pos.add(stop.subtract(bolt.pos).scale(d));
                level.sendParticles(particle(bolt.spell.element), p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            }
            if (struck != null || block.getType() != HitResult.Type.MISS) {
                Vec3 at = struck != null ? struck.position() : stop;
                if (struck != null) strike(owner, struck, bolt.spell.element, bolt.spell.element.damage * bolt.spell.shape.damage * bolt.scale);
                level.sendParticles(impactParticle(bolt.spell.element), at.x, at.y + 0.3, at.z, 12, 0.3, 0.3, 0.3, 0.05);
                level.playSound(null, at.x, at.y, at.z, impactSound(bolt.spell.element), SoundSource.PLAYERS, 0.8f, 1.2f);
                if (bolt.spell.modifier == Modifier.LINGERING) {
                    ZONES.add(new Zone(level, bolt.owner, bolt.spell.element, at, 2.5, bolt.spell.element.damage * 0.3f, 100));
                }
                it.remove();
                continue;
            }
            bolt.pos = next;
        }
    }

    private static void tickZones(ServerLevel level) {
        Iterator<Zone> it = ZONES.iterator();
        while (it.hasNext()) {
            Zone zone = it.next();
            if (zone.level != level) continue;
            if (--zone.ticksLeft <= 0) {
                it.remove();
                continue;
            }
            if (zone.ticksLeft % 4 == 0) {
                for (int i = 0; i < 6; i++) {
                    double a = level.random.nextDouble() * Math.PI * 2, r = level.random.nextDouble() * zone.radius;
                    level.sendParticles(particle(zone.element), zone.center.x + Math.cos(a) * r, zone.center.y + 0.2,
                        zone.center.z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
                }
            }
            if (zone.ticksLeft % 10 == 0) {
                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(zone.owner);
                if (owner == null) continue;
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(zone.center, zone.radius * 2, 3, zone.radius * 2),
                        e -> canHarm(e, owner) && e.position().distanceTo(zone.center) <= zone.radius + 0.5)) {
                    strike(owner, target, zone.element, zone.damage);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        BOLTS.clear();
        ZONES.clear();
        DELAYED.clear();
    }

    // ---------------- Helpers ----------------

    /** Crafted spells never hurt you, other players, your pets, or armor stands. */
    private static boolean canHarm(Entity entity, ServerPlayer owner) {
        return entity instanceof LivingEntity living && living.isAlive() && living != owner && !(living instanceof Player)
            && !(living instanceof ArmorStand) && !(living instanceof TamableAnimal tame && tame.isOwnedBy(owner));
    }

    /** The closest monster, optionally only within 35 degrees of a direction. */
    private static LivingEntity nearestEnemy(ServerLevel level, ServerPlayer owner, Vec3 from, double range, Vec3 cone) {
        LivingEntity best = null;
        double bestDist = range * range;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(from, range * 2, range * 2, range * 2),
                e -> canHarm(e, owner) && e instanceof Enemy)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            if (cone != null && to.normalize().dot(cone) < 0.82) continue;
            double dist = to.lengthSqr();
            if (dist < bestDist) {
                bestDist = dist;
                best = e;
            }
        }
        return best;
    }

    private static void withOwner(ServerLevel level, UUID id, java.util.function.Consumer<ServerPlayer> action) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(id);
        if (owner != null) action.accept(owner);
    }

    private static ParticleOptions particle(Element element) {
        return switch (element) {
            case FIRE -> HollowmancyParticles.EMBER.get();
            case FROST -> HollowmancyParticles.FROST_SHARD.get();
            case VOID -> HollowmancyParticles.VOID_WISP.get();
        };
    }

    private static ParticleOptions impactParticle(Element element) {
        return switch (element) {
            case FIRE -> HollowmancyParticles.RUNE_FIRE.get();
            case FROST -> HollowmancyParticles.RUNE_FROST.get();
            case VOID -> HollowmancyParticles.RUNE_VOID.get();
        };
    }

    private static SoundEvent castSound(Element element) {
        return switch (element) {
            case FIRE -> SoundEvents.BLAZE_SHOOT;
            case FROST -> SoundEvents.PLAYER_HURT_FREEZE;
            case VOID -> SoundEvents.ENDERMAN_TELEPORT;
        };
    }

    private static SoundEvent impactSound(Element element) {
        return switch (element) {
            case FIRE -> SoundEvents.FIRECHARGE_USE;
            case FROST -> SoundEvents.GLASS_BREAK;
            case VOID -> SoundEvents.WARDEN_SONIC_BOOM;
        };
    }
}
