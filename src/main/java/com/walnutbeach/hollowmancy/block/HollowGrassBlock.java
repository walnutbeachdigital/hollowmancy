package com.walnutbeach.hollowmancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** End stone with a living violet top. Creeps onto nearby End stone; fades back if covered. */
public class HollowGrassBlock extends Block {

    public HollowGrassBlock(Properties properties) {
        super(properties);
    }

    // Called now and then by Minecraft's random block ticks (needs .randomTicks() in its properties)
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayGrassy(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
            return;
        }
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (level.getBlockState(target).is(Blocks.END_STONE) && canStayGrassy(level, target)) {
                level.setBlockAndUpdate(target, defaultBlockState());
            }
        }
    }

    /** Grass needs open space above it: anything solid on top smothers it. */
    private static boolean canStayGrassy(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return !level.getBlockState(above).isSolidRender(level, above);
    }
}