package com.walnutbeach.hollowmancy.fx;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Hollowmancy's own particles. Textures: assets/hollowmancy/textures/particle, lists: assets/hollowmancy/particles.
 * How each one moves (life, size, drift, glow) is set in client/fx/HollowmancyParticleProviders.
 */
public final class HollowmancyParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Hollowmancy.MODID);

    // ---- Motes and sparks ----
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ARCANE_SPARK = simple("arcane_spark");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER = simple("ember");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_SHARD = simple("frost_shard");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STORM_SPARK = simple("storm_spark");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WARD_GLINT = simple("ward_glint");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DIVINE_MOTE = simple("divine_mote");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LEAF = simple("leaf");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HEAL_MOTE = simple("heal_mote");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_WISP = simple("void_wisp");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_MOTE = simple("water_mote");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WIND = simple("wind");

    // ---- Floating rune glyphs, one colour per kind of magic ----
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_ARCANE = simple("rune_arcane");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_FIRE = simple("rune_fire");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_FROST = simple("rune_frost");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_STORM = simple("rune_storm");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_WARD = simple("rune_ward");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_DIVINE = simple("rune_divine");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_NATURE = simple("rune_nature");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_VOID = simple("rune_void");

    // ---- Spell circles that spread flat across the ground (use SpellFx.ring to send them) ----
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_ARCANE = simple("ring_arcane");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_FIRE = simple("ring_fire");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_FROST = simple("ring_frost");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_STORM = simple("ring_storm");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_WARD = simple("ring_ward");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_DIVINE = simple("ring_divine");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_NATURE = simple("ring_nature");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RING_VOID = simple("ring_void");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> simple(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }

    /** Called from Hollowmancy's constructor. */
    public static void register(IEventBus modEventBus) {
        PARTICLES.register(modEventBus);
    }

    private HollowmancyParticles() {}
}
