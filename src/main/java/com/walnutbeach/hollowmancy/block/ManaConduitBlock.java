package com.walnutbeach.hollowmancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ManaConduitBlock extends Block {

    private static final Map<Direction, BooleanProperty> SIDES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private final Map<BlockState, VoxelShape> shapeCache = new HashMap<>();

    static {
        SIDES.put(Direction.NORTH, BlockStateProperties.NORTH);
        SIDES.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        SIDES.put(Direction.EAST, BlockStateProperties.EAST);
        SIDES.put(Direction.WEST, BlockStateProperties.WEST);
        SIDES.put(Direction.UP, BlockStateProperties.UP);
        SIDES.put(Direction.DOWN, BlockStateProperties.DOWN);

        ARMS.put(Direction.NORTH, Block.box(6, 6, 0, 10, 10, 5));
        ARMS.put(Direction.SOUTH, Block.box(6, 6, 11, 10, 10, 16));
        ARMS.put(Direction.WEST, Block.box(0, 6, 6, 5, 10, 10));
        ARMS.put(Direction.EAST, Block.box(11, 6, 6, 16, 10, 10));
        ARMS.put(Direction.UP, Block.box(6, 11, 6, 10, 16, 10));
        ARMS.put(Direction.DOWN, Block.box(6, 0, 6, 10, 5, 10));
    }

    public ManaConduitBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty side : SIDES.values()) state = state.setValue(side, false);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.EAST,
                BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
    }

    /** What a conduit joins up with. */
    public static boolean connectsTo(BlockState neighbor) {
        Block block = neighbor.getBlock();
        return block instanceof ManaConduitBlock || block instanceof ManaBatteryBlock
                || block instanceof WardingLanternBlock || block instanceof ManaPowered;
    }

    // Work out connections when placed...
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Map.Entry<Direction, BooleanProperty> side : SIDES.entrySet()) {
            BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(side.getKey()));
            state = state.setValue(side.getValue(), connectsTo(neighbor));
        }
        return state;
    }

    // ...and whenever a neighbor changes
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SIDES.get(direction), connectsTo(neighborState));
    }

    // The hitbox matches the visible pipe
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeCache.computeIfAbsent(state, s -> {
            VoxelShape shape = CORE;
            for (Map.Entry<Direction, BooleanProperty> side : SIDES.entrySet()) {
                if (s.getValue(side.getValue())) shape = Shapes.or(shape, ARMS.get(side.getKey()));
            }
            return shape;
        });
    }
}