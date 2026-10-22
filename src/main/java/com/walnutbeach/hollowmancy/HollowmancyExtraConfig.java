package com.walnutbeach.hollowmancy;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings for the newer parts of Hollowmancy.
 *   config/hollowmancy-extra-common.toml : gameplay (shared by the server and everyone on it)
 *   config/hollowmancy-client.toml       : how things look on your screen
 */
public final class HollowmancyExtraConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.DoubleValue HOSTILE_SPAWN_RATE;
    public static final ModConfigSpec.DoubleValue AMBIENT_SPAWN_RATE;
    public static final ModConfigSpec.DoubleValue WAND_MASTERY_SPEED;
    public static final ModConfigSpec.DoubleValue LANDMARK_FREQUENCY;
    public static final ModConfigSpec.BooleanValue ARCHMAGE_AURA;
    public static final ModConfigSpec.BooleanValue MOTH_PICKS_FLOWERS;

    public static final ModConfigSpec.DoubleValue PARTICLE_DENSITY;
    public static final ModConfigSpec.BooleanValue REDUCED_FLASHING;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.push("hollow");
        HOSTILE_SPAWN_RATE = common.comment("How many of the Hollow's hostile creatures spawn naturally (1.0 = all, 0.5 = half, 0 = none).")
                .defineInRange("hostileSpawnRate", 1.0, 0.0, 1.0);
        AMBIENT_SPAWN_RATE = common.comment("The same for wisps, moths, minnows and glimmerhorns.")
                .defineInRange("ambientSpawnRate", 1.0, 0.0, 1.0);
        LANDMARK_FREQUENCY = common.comment("How often observatories, the sunken library and ruins appear in newly explored land (1.0 = normal, 2.0 = twice as often).")
                .defineInRange("landmarkFrequency", 1.0, 0.25, 4.0);
        common.pop();
        common.push("magic");
        WAND_MASTERY_SPEED = common.comment("How fast wands grow with use (1.0 = normal, 2.0 = twice as fast).")
                .defineInRange("wandMasterySpeed", 1.0, 0.1, 10.0);
        ARCHMAGE_AURA = common.comment("Archmages are surrounded by a faint aura in the colour of the magic they last cast. Each player can also turn theirs off with /hollowmancy aura.")
                .define("archmageAura", true);
        MOTH_PICKS_FLOWERS = common.comment("The Crystal Moth familiar picks flowers near you and brings them over. Turn off to keep your gardens safe.")
                .define("mothPicksFlowers", true);
        common.pop();
        COMMON_SPEC = common.build();

        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        client.push("visuals");
        PARTICLE_DENSITY = client.comment("How many of Hollowmancy's particles you see (1.0 = all, 0.25 = a quarter). Spell circles always show.")
                .defineInRange("particleDensity", 1.0, 0.0, 1.0);
        REDUCED_FLASHING = client.comment("Lightning beams hold still instead of flickering, and beams don't pulse.")
                .define("reducedFlashing", false);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    /** A value, or the default if the config hasn't loaded yet. */
    public static double get(ModConfigSpec.DoubleValue value) {
        try { return value.get(); } catch (RuntimeException notLoaded) { return value.getDefault(); }
    }

    public static boolean get(ModConfigSpec.BooleanValue value) {
        try { return value.get(); } catch (RuntimeException notLoaded) { return value.getDefault(); }
    }

    private HollowmancyExtraConfig() {}
}
