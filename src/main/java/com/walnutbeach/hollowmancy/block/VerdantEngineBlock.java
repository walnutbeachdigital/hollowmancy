package com.walnutbeach.hollowmancy.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VerdantEngineBlock extends ManaEngineBlock {

    public VerdantEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VerdantEngineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.VERDANT_ENGINE.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((VerdantEngineBlockEntity) blockEntity).serverTick();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        int mana = level.getBlockEntity(pos) instanceof VerdantEngineBlockEntity engine ? engine.getMana() : 0;
        player.displayClientMessage(state.getValue(ACTIVE)
                ? Component.translatable("hollowmancy.verdant_engine_block.verdant_engine_tending_everything_within", VerdantEngineBlockEntity.RADIUS, mana).withStyle(ChatFormatting.GREEN)
                : Component.translatable("hollowmancy.verdant_engine_block.verdant_engine_idle_connect_it_to").withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }
}