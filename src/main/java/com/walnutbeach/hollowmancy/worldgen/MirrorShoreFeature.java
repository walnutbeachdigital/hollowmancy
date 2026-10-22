package com.walnutbeach.hollowmancy.worldgen;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Runs in the biomes around the Mirror Sea. Every land column near the sea gets solid ground:
 * a pale beach one block above the water, solid right down to the sea bed close to the water,
 * rising into a shelf of land further back. So the water always meets land, never the void.
 */
public class MirrorShoreFeature extends Feature<NoneFeatureConfiguration> {

    /** Within this many blocks of the water, the land is solid all the way down to the sea bed. */
    private static final int WALL = 6;
    /** How far to look for sea (the shore is at most about 28 blocks wide). */
    private static final int REACH = 32;
    /** The biome map works in 4x4 block cells; one sample per cell is exact. */
    private static final int CELL = 4;

    public MirrorShoreFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int x0 = context.origin().getX() & ~15;
        int z0 = context.origin().getZ() & ~15;

        // Which 4x4 cells around this chunk are sea? (x0 is a multiple of 16, so these line up with the cells)
        int n = (16 + 2 * REACH) / CELL;
        boolean[][] sea = new boolean[n][n];
        boolean any = false;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sea[i][j] = MirrorSea.isMirrorSea(level, x0 - REACH + i * CELL, z0 - REACH + j * CELL);
                any |= sea[i][j];
            }
        }
        if (!any) return false;

        boolean grove = level.getBiome(new BlockPos(x0 + 8, MirrorSeaFeature.SURFACE, z0 + 8)).is(MirrorSea.GROVE);
        BlockState topSoil = grove ? Hollowmancy.HOLLOW_GRASS.get().defaultBlockState() : Blocks.END_STONE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                if (sea[(REACH + dx) / CELL][(REACH + dz) / CELL]) continue;   // this column is sea itself

                // Distance from this column to the nearest edge of the water (exact, cell by cell)
                double distance = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    int cx = x0 - REACH + i * CELL;
                    int ox = Math.max(Math.max(cx - x, x - (cx + CELL - 1)), 0);
                    if (ox >= distance) continue;
                    for (int j = 0; j < n; j++) {
                        if (!sea[i][j]) continue;
                        int cz = z0 - REACH + j * CELL;
                        int oz = Math.max(Math.max(cz - z, z - (cz + CELL - 1)), 0);
                        distance = Math.min(distance, Math.sqrt(ox * ox + oz * oz));
                    }
                }

                // A wavy inland edge, 16 to 28 blocks from the water
                double width = 22 + 6 * Math.sin(x * 0.071 + z * 0.023) * Math.cos(z * 0.059 - x * 0.031);
                if (distance > width) continue;

                double t = Mth.clamp((distance - 1) / (width - 1), 0, 1);
                // One block above the water at the beach, rising gently inland, then tapering at the far edge
                int top = MirrorSeaFeature.SURFACE + 1 + (int) Math.round(4 * Math.sin(Math.min(t * 1.6, 1.0) * Math.PI / 2)
                        * (t > 0.75 ? 1 - (t - 0.75) / 0.25 : 1));
                int bottom = distance <= WALL ? MirrorSeaFeature.FLOOR
                    : MirrorSeaFeature.FLOOR + (int) Math.round((top - MirrorSeaFeature.FLOOR)
                        * Math.pow((distance - WALL) / (width - WALL), 0.7));
                bottom = Mth.clamp(bottom, MirrorSeaFeature.FLOOR, top);
                boolean beach = distance < 6;

                for (int y = bottom; y <= top; y++) {
                    pos.set(x, y, z);
                    if (!level.getBlockState(pos).isAir()) continue;   // real islands stay as they are
                    BlockState state;
                    if (y == top) state = beach ? Blocks.CALCITE.defaultBlockState() : topSoil;
                    else if (beach && y >= top - 3) state = Blocks.CALCITE.defaultBlockState();
                    else state = Blocks.END_STONE.defaultBlockState();
                    level.setBlock(pos, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
