package com.walnutbeach.hollowmancy.worldgen;

import com.walnutbeach.hollowmancy.Hollowmancy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Raises hills and mountains on the Hollow's islands (the plain Hollow, Crystal Barrens and Hollow Grove).
 * Rolling hills everywhere, ranges of ridged mountains in places. They fade toward island edges (no peaks
 * hanging over the void), near the Mirror Sea (the beaches stay low) and around structures (nothing gets
 * buried). Each column keeps its own surface block, so every biome looks like itself on the slopes.
 */
public class HollowHillsFeature extends Feature<NoneFeatureConfiguration> {

    /** How the hills look in each biome: rolling hill height, mountain height, how sharp the peaks are. */
    private record Shape(double rolling, double mountains, double sharpness) {}

    private static Shape shapeFor(String biome) {
        return switch (biome) {
            case "crystal_barrens" -> new Shape(6.0, 46.0, 1.5);   // jagged spires
            case "hollow_grove" -> new Shape(7.0, 16.0, 2.6);      // soft, low hills for the trees
            default -> new Shape(8.0, 40.0, 2.2);                   // the plain Hollow: hills and ranges
        };
    }

    private static final int CLEAR = 4, BLEND = 12;   // around structures: flat, then blending back
    private static final int SEA_REACH = 40, CELL = 4;

    private static volatile Noise noise;

    private record Noise(long seed, SimplexNoise rolling, SimplexNoise ranges, SimplexNoise ridges, SimplexNoise detail) {
        static Noise of(long seed) {
            return new Noise(seed,
                new SimplexNoise(RandomSource.create(seed ^ 0x4111L)),
                new SimplexNoise(RandomSource.create(seed ^ 0x4222L)),
                new SimplexNoise(RandomSource.create(seed ^ 0x4333L)),
                new SimplexNoise(RandomSource.create(seed ^ 0x4444L)));
        }
    }

    public HollowHillsFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static double smooth(double edge0, double edge1, double x) {
        double t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
        return t * t * (3 - 2 * t);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int x0 = context.origin().getX() & ~15;
        int z0 = context.origin().getZ() & ~15;
        Noise n = noise;
        if (n == null || n.seed != level.getSeed()) noise = n = Noise.of(level.getSeed());

        // Nearby structures (so they aren't buried) and nearby sea (so beaches stay low)
        List<BoundingBox> structures = structuresNear(level, x0, z0);
        int cells = (16 + 2 * SEA_REACH) / CELL;
        boolean[][] sea = new boolean[cells][cells];
        boolean anySea = false;
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                sea[i][j] = MirrorSea.isMirrorSea(level, x0 - SEA_REACH + i * CELL, z0 - SEA_REACH + j * CELL);
                anySea |= sea[i][j];
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                if (sea[(SEA_REACH + dx) / CELL][(SEA_REACH + dz) / CELL]) continue;

                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;   // the island's top block
                if (top <= level.getMinBuildHeight() + 1) continue;                    // void here
                pos.set(x, top, z);
                BlockState surface = level.getBlockState(pos);
                if (surface.isAir() || !surface.getFluidState().isEmpty()) continue;

                // How thick is the island here? Thin = near an edge, so the hills fade out
                int thickness = 0;
                while (thickness < 40) {
                    pos.set(x, top - thickness - 1, z);
                    if (level.getBlockState(pos).isAir()) break;
                    thickness++;
                }
                double edge = smooth(4, 26, thickness);
                if (edge <= 0) continue;

                String biome = level.getBiome(pos.set(x, top, z)).unwrapKey().map(k -> k.location().getPath()).orElse("");
                Shape shape = shapeFor(biome);

                // Rolling hills, plus ridged mountains where the range noise is high
                double rolling = shape.rolling * (n.rolling.getValue(x / 70.0, z / 70.0) * 0.5 + 0.5);
                double range = smooth(0.0, 0.4, n.ranges.getValue(x / 420.0, z / 420.0));
                double ridge = Math.pow(1.0 - Math.abs(n.ridges.getValue(x / 95.0, z / 95.0)), shape.sharpness);
                double height = rolling + shape.mountains * range * ridge + 1.5 * n.detail.getValue(x / 16.0, z / 16.0);

                // Fade near the sea, around structures and toward island edges
                double fade = edge * clearance(structures, x, z);
                if (anySea) fade *= smooth(6, SEA_REACH, distanceToSea(sea, x0, z0, x, z));
                int rise = (int) Math.round(Math.max(0, height) * fade);
                if (rise <= 0) continue;

                // Build it up: the biome's own surface on top, a few layers of what lies under it, then end stone
                pos.set(x, top - 1, z);
                BlockState under = level.getBlockState(pos);
                if (under.isAir()) under = Blocks.END_STONE.defaultBlockState();
                for (int y = top; y < top + rise; y++) {
                    pos.set(x, y, z);
                    BlockState state = (top + rise - 1 - y) < 3 ? under : Blocks.END_STONE.defaultBlockState();
                    level.setBlock(pos, state, 2);
                }
                pos.set(x, top + rise, z);
                if (level.getBlockState(pos).isAir()) level.setBlock(pos, surface, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static double distanceToSea(boolean[][] sea, int x0, int z0, int x, int z) {
        double best = Double.MAX_VALUE;
        for (int i = 0; i < sea.length; i++) {
            int cx = x0 - SEA_REACH + i * CELL;
            int ox = Math.max(Math.max(cx - x, x - (cx + CELL - 1)), 0);
            if (ox >= best) continue;
            for (int j = 0; j < sea[i].length; j++) {
                if (!sea[i][j]) continue;
                int cz = z0 - SEA_REACH + j * CELL;
                int oz = Math.max(Math.max(cz - z, z - (cz + CELL - 1)), 0);
                best = Math.min(best, Math.sqrt(ox * ox + oz * oz));
            }
        }
        return best;
    }

    private static List<BoundingBox> structuresNear(WorldGenLevel level, int x0, int z0) {
        List<BoundingBox> boxes = new ArrayList<>();
        if (!(level instanceof WorldGenRegion region)) return boxes;
        try {
            StructureManager manager = region.getLevel().structureManager().forWorldGenRegion(region);
            for (StructureStart start : manager.startsForStructure(new ChunkPos(x0 >> 4, z0 >> 4), structure -> true)) {
                boxes.add(start.getBoundingBox());
            }
        } catch (RuntimeException ignored) {
            // If the game won't say, the hills just aren't flattened here
        }
        return boxes;
    }

    private static double clearance(List<BoundingBox> boxes, int x, int z) {
        double best = 1.0;
        for (BoundingBox box : boxes) {
            int ox = Math.max(Math.max(box.minX() - x, x - box.maxX()), 0);
            int oz = Math.max(Math.max(box.minZ() - z, z - box.maxZ()), 0);
            best = Math.min(best, smooth(CLEAR, CLEAR + BLEND, Math.sqrt(ox * ox + oz * oz)));
        }
        return best;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.FEATURE, helper ->
                helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "hollow_hills"), new HollowHillsFeature()));
        }
    }
}
