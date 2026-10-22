package com.walnutbeach.hollowmancy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public class RitualParts {

    public static final Predicate<BlockState> LIT_CANDLE = state ->
            state.is(BlockTags.CANDLES) && state.hasProperty(CandleBlock.LIT) && state.getValue(CandleBlock.LIT);

    public static final Predicate<BlockState> AMETHYST_BLOCK = state -> state.is(Blocks.AMETHYST_BLOCK);

    /** North, south, east and west of the altar, at the altar's height. */
    public static List<BlockPos> cardinals(int distance) {
        return List.of(
                new BlockPos(distance, 0, 0), new BlockPos(-distance, 0, 0),
                new BlockPos(0, 0, distance), new BlockPos(0, 0, -distance));
    }

    /** The four diagonal corners around the altar, at the altar's height. */
    public static List<BlockPos> diagonals(int distance) {
        return List.of(
                new BlockPos(distance, 0, distance), new BlockPos(distance, 0, -distance),
                new BlockPos(-distance, 0, distance), new BlockPos(-distance, 0, -distance));
    }
}