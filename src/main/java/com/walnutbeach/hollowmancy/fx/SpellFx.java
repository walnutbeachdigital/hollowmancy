package com.walnutbeach.hollowmancy.fx;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The spell visual toolkit. Everything here runs on the server and is sent to nearby players.
 *   burst     - a puff of particles
 *   ring      - a spell circle spreading flat over the ground
 *   runeCircle- glyphs rising in a circle
 *   beam      - a glowing beam between two points (smooth, or flickering lightning)
 *   trail     - particles following a projectile, with a burst where it lands
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class SpellFx {
    // ---- Beam colours ----
    public static final int ARCANE = 0xC9A0FF;
    public static final int FIRE = 0xFF8A3D;
    public static final int FROST = 0x9AE8FF;
    public static final int STORM = 0x8FD8FF;
    public static final int WARD = 0x7AB8FF;
    public static final int DIVINE = 0xFFD86A;
    public static final int NATURE = 0x8AE070;
    public static final int VOID = 0xB24BFF;

    // ---- Beam styles ----
    public static final int SMOOTH = 0;
    public static final int LIGHTNING = 1;
    /** A glowing pulse that travels from `from` to `to` over its life (mana flowing through conduits). */
    public static final int FLOW = 2;
    /** Mana pulses travel this many blocks per tick. */
    public static final double FLOW_SPEED = 0.45;

    /** How far away players still see beams. */
    private static final double BEAM_VIEW_RANGE = 96.0;

    // ---------------- Picking particles by school ----------------

    public static ParticleOptions spark(MagicSchool school) {
        return switch (school) {
            case ELEMENTAL -> HollowmancyParticles.EMBER.get();
            case WARDING -> HollowmancyParticles.WARD_GLINT.get();
            case DIVINATION -> HollowmancyParticles.DIVINE_MOTE.get();
            case NATURE -> HollowmancyParticles.HEAL_MOTE.get();
            case FORBIDDEN -> HollowmancyParticles.VOID_WISP.get();
            default -> HollowmancyParticles.ARCANE_SPARK.get();
        };
    }

    public static ParticleOptions rune(MagicSchool school) {
        return switch (school) {
            case ELEMENTAL -> HollowmancyParticles.RUNE_FIRE.get();
            case WARDING -> HollowmancyParticles.RUNE_WARD.get();
            case DIVINATION -> HollowmancyParticles.RUNE_DIVINE.get();
            case NATURE -> HollowmancyParticles.RUNE_NATURE.get();
            case FORBIDDEN -> HollowmancyParticles.RUNE_VOID.get();
            default -> HollowmancyParticles.RUNE_ARCANE.get();
        };
    }

    public static ParticleOptions ringFor(MagicSchool school) {
        return switch (school) {
            case ELEMENTAL -> HollowmancyParticles.RING_FIRE.get();
            case WARDING -> HollowmancyParticles.RING_WARD.get();
            case DIVINATION -> HollowmancyParticles.RING_DIVINE.get();
            case NATURE -> HollowmancyParticles.RING_NATURE.get();
            case FORBIDDEN -> HollowmancyParticles.RING_VOID.get();
            default -> HollowmancyParticles.RING_ARCANE.get();
        };
    }

    // ---------------- Effects ----------------

    public static void burst(ServerLevel level, ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** A circle that spreads out flat to `radius` blocks. Use one of the RING_ particles. */
    public static void ring(ServerLevel level, ParticleOptions ring, Vec3 center, float radius) {
        // count 0 sends the numbers through exactly: the ring reads its size from them
        level.sendParticles(ring, center.x, center.y, center.z, 0, radius, 0.0, 0.0, 1.0);
    }

    /** `count` glyphs rising around a circle. */
    public static void runeCircle(ServerLevel level, ParticleOptions rune, Vec3 center, double radius, int count) {
        double offset = level.random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            double a = offset + i * Math.PI * 2 / count;
            level.sendParticles(rune, center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius,
                    0, 0.0, 0.03, 0.0, 1.0);
        }
    }

    /** A ring plus a few glyphs at someone's feet: the "a spell was cast here" flourish. */
    public static void flourish(ServerLevel level, MagicSchool school, Vec3 feet, float radius) {
        ring(level, ringFor(school), feet.add(0, 0.08, 0), radius);
        runeCircle(level, rune(school), feet.add(0, 0.2, 0), radius * 0.55, 5);
    }

    /** A glowing beam. width in blocks (0.1 is thin), life in ticks. */
    public static void beam(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int life, int style) {
        Vec3 mid = from.add(to).scale(0.5);
        PacketDistributor.sendToPlayersNear(level, null, mid.x, mid.y, mid.z, BEAM_VIEW_RANGE,
                new BeamPayload(from, to, color, width, life, style));
    }

    /**
     * A pulse of mana running along a path of points (battery, conduits..., machine), one straight run at a time.
     * Each run waits for the one before it, so the pulse travels the whole way.
     */
    public static void flow(ServerLevel level, java.util.List<Vec3> path, int color, float width) {
        double delay = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            Vec3 a = path.get(i), b = path.get(i + 1);
            int life = Math.max(2, (int) Math.ceil(a.distanceTo(b) / FLOW_SPEED));
            // the style carries the start delay in its upper bits
            beam(level, a, b, color, width, life, FLOW | ((int) delay << 8));
            delay += life;
        }
    }

    // ---------------- Projectile trails ----------------

    private record Trail(ParticleOptions trail, ParticleOptions impact, long until) {}
    private static final Map<Entity, Trail> TRAILS = new IdentityHashMap<>();
    private static final Map<Entity, Vec3> LAST_SEEN = new IdentityHashMap<>();

    /** Particles follow the projectile for up to 10 seconds; `impact` bursts where it ends (null for none). */
    public static void trail(Entity projectile, ParticleOptions trail, ParticleOptions impact) {
        TRAILS.put(projectile, new Trail(trail, impact, projectile.level().getGameTime() + 200));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TRAILS.isEmpty()) return;
        for (Map.Entry<Entity, Trail> entry : new ArrayList<>(TRAILS.entrySet())) {
            Entity e = entry.getKey();
            Trail t = entry.getValue();
            if (!(e.level() instanceof ServerLevel level)) { TRAILS.remove(e); LAST_SEEN.remove(e); continue; }
            if (e.isRemoved() || level.getGameTime() > t.until()) {
                Vec3 at = LAST_SEEN.getOrDefault(e, e.position());
                if (t.impact() != null) {
                    level.sendParticles(t.impact(), at.x, at.y, at.z, 14, 0.25, 0.25, 0.25, 0.06);
                    level.sendParticles(t.trail(), at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.08);
                }
                TRAILS.remove(e);
                LAST_SEEN.remove(e);
                continue;
            }
            Vec3 now = e.position().add(0, e.getBbHeight() / 2, 0);
            Vec3 before = LAST_SEEN.getOrDefault(e, now);
            for (int i = 0; i < 3; i++) {
                Vec3 p = before.lerp(now, i / 3.0);
                level.sendParticles(t.trail(), p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.01);
            }
            LAST_SEEN.put(e, now);
        }
    }

    private SpellFx() {}
}
