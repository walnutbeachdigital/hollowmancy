package com.walnutbeach.hollowmancy.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Base for mana-powered machines: a glowing top while working. */
public abstract class ManaEngineBlock extends Block implements EntityBlock, ManaPowered {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected ManaEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}