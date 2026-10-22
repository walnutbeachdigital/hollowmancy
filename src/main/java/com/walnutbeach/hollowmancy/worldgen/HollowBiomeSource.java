package com.walnutbeach.hollowmancy.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.walnutbeach.hollowmancy.Hollowmancy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * The Hollow's biome layout: irregular regions (about cell_size blocks across) whose borders are bent by
 * two layers of noise, so they wander and curl instead of running in straight lines. Each world seed
 * gives a different layout. The area around the arrival point is always the plain Hollow.
 *
 * In data/hollowmancy/dimension/the_hollow.json:
 *   "biome_source": { "type": "hollowmancy:hollow", "biomes": [ ... ], "cell_size": 288 }
 */
public class HollowBiomeSource extends BiomeSource {

    public static final MapCodec<HollowBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(source -> source.biomes),
            Codec.INT.optionalFieldOf("cell_size", 288).forGetter(source -> source.cellSize)
    ).apply(instance, HollowBiomeSource::new));

    /** How common each biome is (others count as 2). */
    private static int weight(Holder<Biome> biome) {
        String id = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");
        return switch (id) {
            case "the_hollow" -> 4;
            case "crystal_barrens" -> 3;
            case "hollow_grove" -> 3;
            case "mirror_sea" -> 2;
            case "hollow_depths" -> 0;   // only used below DEPTHS_BELOW
            default -> 2;
        };
    }

    /** Blocks from the centre that are always the plain Hollow (the arrival island). */
    private static final int HOME_RADIUS = 320;

    private static volatile long worldSeed = 0;

    private final HolderSet<Biome> biomes;
    private final int cellSize;
    private final List<Holder<Biome>> weighted = new ArrayList<>();
    private final Holder<Biome> home;
    /** Everything below this height is the depths: nothing spawns or is built there. */
    private static final int DEPTHS_BELOW = 12;
    private final Holder<Biome> depths;
    private volatile Noise noise;

    private record Noise(long seed, SimplexNoise warpX, SimplexNoise warpZ, SimplexNoise fineX, SimplexNoise fineZ) {
        static Noise of(long seed) {
            return new Noise(seed,
                    new SimplexNoise(RandomSource.create(seed ^ 0x51A7E5L)),
                    new SimplexNoise(RandomSource.create(seed ^ 0x2B7E1516L)),
                    new SimplexNoise(RandomSource.create(seed ^ 0x7F4A7C15L)),
                    new SimplexNoise(RandomSource.create(seed ^ 0x6A09E667L)));
        }
    }

    public HollowBiomeSource(HolderSet<Biome> biomes, int cellSize) {
        this.biomes = biomes;
        this.cellSize = Math.max(64, cellSize);
        for (Holder<Biome> biome : biomes) {
            for (int i = 0; i < weight(biome); i++) weighted.add(biome);
        }
        this.home = biomes.size() > 0 ? biomes.get(0) : null;
        Holder<Biome> deep = null;
        for (Holder<Biome> biome : biomes) {
            if (biome.unwrapKey().map(k -> k.location().getPath().equals("hollow_depths")).orElse(false)) deep = biome;
        }
        this.depths = deep;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }

    private Noise noise() {
        Noise n = noise;
        if (n == null || n.seed != worldSeed) noise = n = Noise.of(worldSeed);
        return n;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        if (depths != null && QuartPos.toBlock(quartY) < DEPTHS_BELOW) return depths;
        int x = QuartPos.toBlock(quartX), z = QuartPos.toBlock(quartZ);

        // Bend space: a broad sweep plus a finer ripple, so borders curve and wiggle
        Noise n = noise();
        double wx = x + 110 * n.warpX.getValue(x / 420.0, z / 420.0) + 28 * n.fineX.getValue(x / 95.0, z / 95.0);
        double wz = z + 110 * n.warpZ.getValue(x / 420.0, z / 420.0) + 28 * n.fineZ.getValue(x / 95.0, z / 95.0);
        if (wx * wx + wz * wz < (double) HOME_RADIUS * HOME_RADIUS) return home;   // the arrival area, with a ragged edge too

        // Nearest of a scattered set of region centres (one per cell, jittered) owns this spot
        int cx = (int) Math.floor(wx / cellSize), cz = (int) Math.floor(wz / cellSize);
        double best = Double.MAX_VALUE;
        long bestHash = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                long h = hash(cx + i, cz + j, n.seed);
                double px = (cx + i + 0.15 + 0.7 * ((h & 0xFFFF) / 65535.0)) * cellSize;
                double pz = (cz + j + 0.15 + 0.7 * (((h >>> 16) & 0xFFFF) / 65535.0)) * cellSize;
                double d = (px - wx) * (px - wx) + (pz - wz) * (pz - wz);
                if (d < best) { best = d; bestHash = h; }
            }
        }
        return weighted.get((int) Long.remainderUnsigned(bestHash >>> 32, weighted.size()));
    }

    private static long hash(int x, int z, long seed) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 33; h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33; h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }

    /** Registers "hollowmancy:hollow" as a biome source type. */
    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.BIOME_SOURCE, helper ->
                    helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "hollow"), CODEC));
        }
    }

    /** Picks up the world seed before any land is made, so each world gets its own layout. */
    @EventBusSubscriber(modid = Hollowmancy.MODID)
    public static class Seed {
        @SubscribeEvent
        public static void onServerStarting(ServerAboutToStartEvent event) {
            worldSeed = event.getServer().getWorldData().worldGenOptions().seed();
        }
    }
}
