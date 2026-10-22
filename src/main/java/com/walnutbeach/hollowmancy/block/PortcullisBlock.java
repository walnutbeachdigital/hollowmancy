package com.walnutbeach.hollowmancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A castle gate. Every portcullis block touching another is one gate: power any of them
 * (a lever beside it, a button, redstone) and the whole gate rises; cut the power and it drops.
 */
public class PortcullisBlock extends Block {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final int MAX_GATE = 96;

    public PortcullisBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                   boolean movedByPiston) {
        if (!level.isClientSide) updateGate(level, pos);
    }

    private void updateGate(Level level, BlockPos start) {
        List<BlockPos> gate = gate(level, start);
        boolean powered = false;
        for (BlockPos pos : gate) {
            if (level.hasNeighborSignal(pos)) {
                powered = true;
                break;
            }
        }
        if (level.getBlockState(start).getValue(OPEN) == powered) return;
        for (BlockPos pos : gate) {
            level.setBlock(pos, level.getBlockState(pos).setValue(OPEN, powered), Block.UPDATE_CLIENTS);
        }
        level.playSound(null, start, powered ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 0.6f);
        level.playSound(null, start, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0f, 0.5f);
    }

    /** Every portcullis block connected to this one. */
    private List<BlockPos> gate(Level level, BlockPos start) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && found.size() < MAX_GATE) {
            BlockPos pos = queue.poll();
            found.add(pos);
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (seen.add(next) && level.getBlockState(next).getBlock() instanceof PortcullisBlock) queue.add(next);
            }
        }
        return found;
    }
}
