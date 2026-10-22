package com.walnutbeach.hollowmancy.worldgen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fills a Mirror Sea chunk: a still sea between the islands, a pale floor of rolling hills and winding
 * valleys (flattened into clearings where ruins stand), and, in the water beneath
 * every island, its reflection, upside-down and real. Its edges are held in by the shore that
 * MirrorShoreFeature raises in the neighbouring biomes.
 */
public class MirrorSeaFeature extends Feature<NoneFeatureConfiguration> {

    public static final int SURFACE = 50;   // the top layer of water: the mirror
    public static final int FLOOR = 18;     // the lowest the sea bed goes (the void lies below)
    /** Hilltops always stay at least this far under the surface. */
    private static final int MIN_DEPTH = 6;
    /** Around ruins the floor is flat for this many blocks, then blends back into the hills over BLEND. */
    private static final int CLEAR = 3, BLEND = 8;
    /** Near land the bed rises into a steep bank: blocks it drops for each block out to sea. */
    private static final double BANK_DROP = 2.6;
    private static final int BANK_REACH = 16, CELL = 4;

    private static volatile Hills hills;

    private record Hills(long seed, SimplexNoise broad, SimplexNoise mid, SimplexNoise fine, SimplexNoise valleys) {
        static Hills of(long seed) {
            return new Hills(seed,
                new SimplexNoise(RandomSource.create(seed ^ 0x5EAF100DL)),
                new SimplexNoise(RandomSource.create(seed ^ 0x1D2C3B4AL)),
                new SimplexNoise(RandomSource.create(seed ^ 0x7E57A11EL)),
                new SimplexNoise(RandomSource.create(seed ^ 0x0BADC0DEL)));
        }

        /** The height of the sea bed's surface here, before ruins are cleared. */
        double height(int x, int z) {
            double h = FLOOR + 7
                + 9.0 * broad.getValue(x / 110.0, z / 110.0)
                + 4.0 * mid.getValue(x / 38.0, z / 38.0)
                + 1.5 * fine.getValue(x / 12.0, z / 12.0);
            // Winding valleys: wherever this noise crosses zero, the floor sinks into a channel
            double v = Math.abs(valleys.getValue(x / 160.0, z / 160.0));
            if (v < 0.14) h -= (0.14 - v) / 0.14 * 10.0;
            return Math.max(FLOOR, Math.min(SURFACE - MIN_DEPTH, h));
        }
    }

    private static Hills hills(long seed) {
        Hills h = hills;
        if (h == null || h.seed != seed) hills = h = Hills.of(seed);
        return h;
    }

    /** The boxes of any ruins or shrines touching this chunk, so the floor can clear a space for them. */
    private static List<BoundingBox> ruinsNear(WorldGenLevel level, int x0, int z0) {
        List<BoundingBox> boxes = new ArrayList<>();
        if (!(level instanceof WorldGenRegion region)) return boxes;
        try {
            StructureManager structures = region.getLevel().structureManager().forWorldGenRegion(region);
            for (StructureStart start : structures.startsForStructure(new ChunkPos(x0 >> 4, z0 >> 4), structure -> true)) {
                BoundingBox box = start.getBoundingBox();
                if (box.minY() < SURFACE && box.maxY() < SURFACE + 16) boxes.add(box);   // only ruins down in the water, not the castle
            }
        } catch (RuntimeException ignored) {
            // If the game won't tell us, the floor just stays hilly here
        }
        return boxes;
    }

    /** 0 inside a ruin's clearing, rising to 1 once you're BLEND blocks clear of it. */
    private static double clearance(List<BoundingBox> boxes, int x, int z) {
        double best = 1.0;
        for (BoundingBox box : boxes) {
            int ox = Math.max(Math.max(box.minX() - x, x - box.maxX()), 0);
            int oz = Math.max(Math.max(box.minZ() - z, z - box.maxZ()), 0);
            double d = Math.sqrt(ox * ox + oz * oz);
            double t = Math.max(0.0, Math.min(1.0, (d - CLEAR) / BLEND));
            best = Math.min(best, t * t * (3 - 2 * t));
        }
        return best;
    }

    public MirrorSeaFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int x0 = context.origin().getX() & ~15;
        int z0 = context.origin().getZ() & ~15;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos source = new BlockPos.MutableBlockPos();
        boolean placed = false;
        Hills hills = hills(level.getSeed());
        List<BoundingBox> ruins = ruinsNear(level, x0, z0);

        // Which 4x4 cells around this chunk are land? (for the banks along the shore)
        int cells = (16 + 2 * BANK_REACH) / CELL;
        boolean[][] land = new boolean[cells][cells];
        boolean nearLand = false;
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                land[i][j] = !MirrorSea.isMirrorSea(level, x0 - BANK_REACH + i * CELL, z0 - BANK_REACH + j * CELL);
                nearLand |= land[i][j];
            }
        }

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                if (!MirrorSea.isMirrorSea(level, x, z)) continue;
                placed = true;

                // The sea bed: solid from the bottom up to the hills' surface, with a pale skin on top
                double clear = clearance(ruins, x, z);
                boolean insideRuin = clear <= 0.0;   // ruins and the shrine get open water: nothing to get stuck in
                int bed = FLOOR + (int) Math.round((hills.height(x, z) - FLOOR) * clear);

                // A steep bank where the sea meets land: just under the surface at the beach, dropping fast
                if (nearLand && !insideRuin) {
                    double toLand = Double.MAX_VALUE;
                    for (int i = 0; i < cells; i++) {
                        int cx = x0 - BANK_REACH + i * CELL;
                        int ox = Math.max(Math.max(cx - x, x - (cx + CELL - 1)), 0);
                        if (ox >= toLand) continue;
                        for (int j = 0; j < cells; j++) {
                            if (!land[i][j]) continue;
                            int cz = z0 - BANK_REACH + j * CELL;
                            int oz = Math.max(Math.max(cz - z, z - (cz + CELL - 1)), 0);
                            toLand = Math.min(toLand, Math.sqrt(ox * ox + oz * oz));
                        }
                    }
                    if (toLand < BANK_REACH) {
                        double drop = BANK_DROP + 0.8 * hills.fine.getValue(x / 9.0, z / 9.0);   // uneven, not machine-made
                        int bank = (int) Math.round(SURFACE - 1 - (toLand - 1) * drop
                            + 1.5 * hills.mid.getValue(x / 14.0 + 50, z / 14.0));
                        bed = Math.max(bed, Math.min(SURFACE - 1, bank));
                    }
                }
                for (int y = FLOOR; y <= bed; y++) {
                    pos.set(x, y, z);
                    if (!level.getBlockState(pos).isAir()) continue;
                    BlockState state = y == bed ? floorBlock(random)
                        : y >= bed - 2 ? Blocks.CALCITE.defaultBlockState()
                        : y >= bed - 5 && random.nextInt(3) == 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                        : Blocks.END_STONE.defaultBlockState();
                    level.setBlock(pos, state, 2);
                }

                for (int y = bed + 1; y <= SURFACE; y++) {
                    pos.set(x, y, z);
                    if (!level.getBlockState(pos).isAir()) continue;   // the island's own underside (and ruins) stay
                    source.set(x, 2 * SURFACE + 1 - y, z);             // the matching block above the mirror
                    level.setBlock(pos, insideRuin ? Blocks.WATER.defaultBlockState() : reflect(level.getBlockState(source)), 2);
                }

                // Glowing sea pickles here and there on the bed
                pos.set(x, bed + 1, z);
                if (random.nextInt(40) == 0 && level.getBlockState(pos).is(Blocks.WATER)) {
                    level.setBlock(pos, Blocks.SEA_PICKLE.defaultBlockState()
                        .setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4))
                        .setValue(BlockStateProperties.WATERLOGGED, true), 2);
                }
            }
        }
        return placed;
    }

    /** What the reflection of a block looks like underwater. */
    private static BlockState reflect(BlockState above) {
        if (above.isAir() || !above.getFluidState().isEmpty()) return Blocks.WATER.defaultBlockState();
        if (above.is(BlockTags.LEAVES) && above.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return above.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        if (above.canOcclude()) return above;                       // stone, logs, crystal: mirrored exactly
        return Blocks.WATER.defaultBlockState();                     // flowers, grass and the like don't reflect
    }

    private static BlockState floorBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 2) return Blocks.SEA_LANTERN.defaultBlockState();
        if (roll < 12) return Blocks.SMOOTH_QUARTZ.defaultBlockState();
        return Blocks.CALCITE.defaultBlockState();
    }
}
