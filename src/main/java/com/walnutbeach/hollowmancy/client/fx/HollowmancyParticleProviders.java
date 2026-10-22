package com.walnutbeach.hollowmancy.client.fx;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** How every Hollowmancy particle looks and moves. Tweak the numbers here (see ArcaneParticle.Look for what they mean). */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HollowmancyParticleProviders {
    private static final int WHITE = 0xFFFFFF;

    //                                         life      size   gravity friction anim   glow   spin   sway  jitter grow   color
    static final ArcaneParticle.Look SPARK  = look(12, 20, 0.12F, 0.0F,   0.88F, true,  true,  0.0F,  0,    0,     -0.3F, WHITE);
    static final ArcaneParticle.Look EMBER  = look(16, 28, 0.09F, -0.03F, 0.92F, true,  true,  0.05F, 0.3F, 0,     -0.4F, WHITE);
    static final ArcaneParticle.Look FROST  = look(28, 44, 0.10F, 0.12F,  0.94F, false, true,  0.15F, 0.2F, 0,     -0.2F, WHITE);
    static final ArcaneParticle.Look STORM  = look(5, 9,   0.10F, 0.0F,   0.80F, false, true,  0.6F,  0,    0.04F, 0.0F,  WHITE);
    static final ArcaneParticle.Look WARD   = look(16, 26, 0.12F, -0.01F, 0.90F, false, true,  0.05F, 0,    0,     -0.2F, WHITE);
    static final ArcaneParticle.Look DIVINE = look(24, 40, 0.07F, -0.03F, 0.94F, true,  true,  0.0F,  0.2F, 0,     0.0F,  WHITE);
    static final ArcaneParticle.Look LEAF   = look(40, 64, 0.10F, 0.04F,  0.96F, false, false, 0.12F, 1.2F, 0,     0.0F,  WHITE);
    static final ArcaneParticle.Look HEAL   = look(20, 30, 0.10F, -0.05F, 0.90F, false, true,  0.0F,  0.3F, 0,     -0.3F, WHITE);
    static final ArcaneParticle.Look VOIDW  = look(20, 32, 0.14F, -0.01F, 0.90F, true,  true,  0.04F, 0.2F, 0,     0.6F,  WHITE);
    static final ArcaneParticle.Look WATER  = look(14, 24, 0.07F, 0.5F,   0.96F, false, false, 0.0F,  0,    0,     -0.3F, WHITE);
    static final ArcaneParticle.Look WIND   = look(10, 16, 0.16F, 0.0F,   0.84F, false, false, 0.0F,  0,    0,     0.4F,  WHITE);

    static ArcaneParticle.Look rune(int color) { return look(30, 46, 0.17F, -0.015F, 0.96F, false, true, 0.03F, 0.1F, 0, -0.2F, color); }
    static ArcaneParticle.Look ring(int color) { return look(13, 16, 1.0F, 0.0F, 1.0F, false, true, 0.0F, 0, 0, 0.0F, color); }

    private static ArcaneParticle.Look look(int lifeMin, int lifeMax, float size, float gravity, float friction, boolean animate, boolean glow,
                                            float spin, float sway, float jitter, float grow, int color) {
        return new ArcaneParticle.Look(lifeMin, lifeMax, size, gravity, friction, animate, glow, spin, sway, jitter, grow, color);
    }

    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent event) {
        simple(event, HollowmancyParticles.ARCANE_SPARK, SPARK);
        simple(event, HollowmancyParticles.EMBER, EMBER);
        simple(event, HollowmancyParticles.FROST_SHARD, FROST);
        simple(event, HollowmancyParticles.STORM_SPARK, STORM);
        simple(event, HollowmancyParticles.WARD_GLINT, WARD);
        simple(event, HollowmancyParticles.DIVINE_MOTE, DIVINE);
        simple(event, HollowmancyParticles.LEAF, LEAF);
        simple(event, HollowmancyParticles.HEAL_MOTE, HEAL);
        simple(event, HollowmancyParticles.VOID_WISP, VOIDW);
        simple(event, HollowmancyParticles.WATER_MOTE, WATER);
        simple(event, HollowmancyParticles.WIND, WIND);

        simple(event, HollowmancyParticles.RUNE_ARCANE, rune(0xC9A0FF));
        simple(event, HollowmancyParticles.RUNE_FIRE, rune(0xFF9A4A));
        simple(event, HollowmancyParticles.RUNE_FROST, rune(0x9AE8FF));
        simple(event, HollowmancyParticles.RUNE_STORM, rune(0xB8F0FF));
        simple(event, HollowmancyParticles.RUNE_WARD, rune(0x7AB8FF));
        simple(event, HollowmancyParticles.RUNE_DIVINE, rune(0xFFD86A));
        simple(event, HollowmancyParticles.RUNE_NATURE, rune(0x8AE070));
        simple(event, HollowmancyParticles.RUNE_VOID, rune(0xE04A8A));

        ring(event, HollowmancyParticles.RING_ARCANE, ring(0xC9A0FF));
        ring(event, HollowmancyParticles.RING_FIRE, ring(0xFF8A3D));
        ring(event, HollowmancyParticles.RING_FROST, ring(0x9AE8FF));
        ring(event, HollowmancyParticles.RING_STORM, ring(0x8FD8FF));
        ring(event, HollowmancyParticles.RING_WARD, ring(0x7AB8FF));
        ring(event, HollowmancyParticles.RING_DIVINE, ring(0xFFD86A));
        ring(event, HollowmancyParticles.RING_NATURE, ring(0x8AE070));
        ring(event, HollowmancyParticles.RING_VOID, ring(0xB24BFF));
    }

    private static void simple(RegisterParticleProvidersEvent event, DeferredHolder<ParticleType<?>, SimpleParticleType> type, ArcaneParticle.Look look) {
        event.registerSpriteSet(type.get(), sprites -> new Provider(sprites, look, false));
    }

    private static void ring(RegisterParticleProvidersEvent event, DeferredHolder<ParticleType<?>, SimpleParticleType> type, ArcaneParticle.Look look) {
        event.registerSpriteSet(type.get(), sprites -> new Provider(sprites, look, true));
    }

    public record Provider(SpriteSet sprites, ArcaneParticle.Look look, boolean ring) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            // the "particleDensity" setting thins everything out except spell circles
            if (!ring && level.random.nextFloat() >= HollowmancyExtraConfig.get(HollowmancyExtraConfig.PARTICLE_DENSITY)) return null;
            return ring ? new ArcaneParticle.Ring(level, x, y, z, xd, sprites, look)
                        : new ArcaneParticle(level, x, y, z, xd, yd, zd, sprites, look);
        }
    }
}
