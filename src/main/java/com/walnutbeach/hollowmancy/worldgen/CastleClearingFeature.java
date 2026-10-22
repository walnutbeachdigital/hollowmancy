package com.walnutbeach.hollowmancy.worldgen;

import com.walnutbeach.hollowmancy.Hollowmancy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Morvane's castle stands on its own floating island. This clears the Hollow's land away from it,
 * leaving open sky at least GAP blocks wide all around the castle's island, so the two never collide.
 * The cleared edge wanders, and its underside slopes back like a real island's, so it doesn't look cut.
 * The castle's island is painted as its own biome, the Revenant Isle (no trees or grass grow through the castle).
 * Runs before the castle is placed, so the castle itself is never touched.
 */
public class CastleClearingFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation CASTLE = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "revenant_castle");
    private static final ResourceKey<Biome> ISLE =
        ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "revenant_isle"));
    /** The castle's island reaches this far from its centre (matches the castle's structure file). */
    private static final int ISLAND_RADIUS = 66;
    /** Open sky between the castle's island and any other land. */
    private static final int GAP = 30;
    /** How much the underside slopes back: blocks outward per block down. */
    private static final double UNDERSIDE = 0.6;
    /** Leave this much land next to the Mirror Sea, so its water stays held in. */
    private static final int SEA_KEEP = 8, CELL = 4;

    private static volatile SimplexNoise edgeNoise;
    private static volatile long edgeSeed;

    public CastleClearingFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int x0 = context.origin().getX() & ~15;
        int z0 = context.origin().getZ() & ~15;
        List<BoundingBox> castles = castlesNear(level, x0, z0);
        if (castles.isEmpty()) return false;

        SimplexNoise noise = edgeNoise;
        if (noise == null || edgeSeed != level.getSeed()) {
            edgeNoise = noise = new SimplexNoise(RandomSource.create(level.getSeed() ^ 0xCA57L));
            edgeSeed = level.getSeed();
        }

        // Mirror Sea nearby? Its banks are kept.
        int cells = (16 + 2 * SEA_KEEP) / CELL;
        boolean[][] sea = new boolean[cells][cells];
        boolean anySea = false;
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                sea[i][j] = MirrorSea.isMirrorSea(level, x0 - SEA_KEEP + i * CELL, z0 - SEA_KEEP + j * CELL);
                anySea |= sea[i][j];
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                if (anySea && seaWithin(sea, x0, z0, x, z)) continue;

                // How close is the nearest castle island here?
                double reach = Double.MAX_VALUE;
                for (BoundingBox box : castles) {
                    double cx = (box.minX() + box.maxX()) / 2.0, cz = (box.minZ() + box.maxZ()) / 2.0;
                    reach = Math.min(reach, Math.hypot(x - cx, z - cz));
                }
                double radius = ISLAND_RADIUS + GAP + 7 * noise.getValue(x / 45.0, z / 45.0);   // wandering edge
                if (reach > radius + 40) continue;

                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                for (int y = level.getMinBuildHeight(); y <= top; y++) {
                    // Deeper down, the land is cleared further out: the cut edge slopes back underneath
                    if (reach > radius + Math.min(15, Math.max(0, top - y) * UNDERSIDE)) continue;   // stays inside the castle's padded frame
                    pos.set(x, y, z);
                    if (level.getBlockState(pos).isAir()) continue;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    changed = true;
                }
            }
        }
        paintIsle(level, x0, z0, castles);
        return changed;
    }

    /** The castle's island becomes the Revenant Isle biome. */
    private static void paintIsle(WorldGenLevel level, int x0, int z0, List<BoundingBox> castles) {
        if (!(level instanceof WorldGenRegion region)) return;
        boolean touches = false;
        for (BoundingBox box : castles) {
            double cx = (box.minX() + box.maxX()) / 2.0, cz = (box.minZ() + box.maxZ()) / 2.0;
            double nx = Math.max(x0, Math.min(cx, x0 + 15)), nz = Math.max(z0, Math.min(cz, z0 + 15));
            touches |= Math.hypot(nx - cx, nz - cz) <= ISLAND_RADIUS + 6;
        }
        if (!touches) return;
        Holder<Biome> isle = level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(ISLE).orElse(null);
        if (isle == null) return;
        try {
            ChunkAccess chunk = level.getChunk(x0 >> 4, z0 >> 4);
            Climate.Sampler sampler = region.getLevel().getChunkSource().randomState().sampler();
            chunk.fillBiomesFromNoise((qx, qy, qz, s) -> {
                int x = QuartPos.toBlock(qx) + 2, z = QuartPos.toBlock(qz) + 2;
                for (BoundingBox box : castles) {
                    double cx = (box.minX() + box.maxX()) / 2.0, cz = (box.minZ() + box.maxZ()) / 2.0;
                    if (Math.hypot(x - cx, z - cz) <= ISLAND_RADIUS + 6) return isle;
                }
                return chunk.getNoiseBiome(qx, qy, qz);
            }, sampler);
            chunk.setUnsaved(true);
        } catch (RuntimeException ignored) {
            // Leave the biome as it was if this chunk can't be repainted
        }
    }

    private static boolean seaWithin(boolean[][] sea, int x0, int z0, int x, int z) {
        for (int i = 0; i < sea.length; i++) {
            int cx = x0 - SEA_KEEP + i * CELL;
            if (Math.max(Math.max(cx - x, x - (cx + CELL - 1)), 0) > SEA_KEEP) continue;
            for (int j = 0; j < sea[i].length; j++) {
                if (!sea[i][j]) continue;
                int cz = z0 - SEA_KEEP + j * CELL;
                int ox = Math.max(Math.max(cx - x, x - (cx + CELL - 1)), 0);
                int oz = Math.max(Math.max(cz - z, z - (cz + CELL - 1)), 0);
                if (ox * ox + oz * oz <= SEA_KEEP * SEA_KEEP) return true;
            }
        }
        return false;
    }

    /** Castles whose clearing might reach this chunk. */
    private static List<BoundingBox> castlesNear(WorldGenLevel level, int x0, int z0) {
        List<BoundingBox> boxes = new ArrayList<>();
        if (!(level instanceof WorldGenRegion region)) return boxes;
        try {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure castle = registry.get(CASTLE);
            if (castle == null) return boxes;
            StructureManager manager = region.getLevel().structureManager().forWorldGenRegion(region);
            for (StructureStart start : manager.startsForStructure(new ChunkPos(x0 >> 4, z0 >> 4), s -> s == castle)) {
                boxes.add(start.getBoundingBox());
            }
        } catch (RuntimeException ignored) {
            // If the game won't say, nothing is cleared here
        }
        return boxes;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.FEATURE, helper ->
                helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "castle_clearing"), new CastleClearingFeature()));
        }
    }
}
