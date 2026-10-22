package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.ritual.Rituals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RitualAltarBlock extends Block {

    public RitualAltarBlock(Properties properties) {
        super(properties);
    }

    // Right-click while holding an item
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (Rituals.findByOffering(stack) == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
            Rituals.activate(serverPlayer, (ServerLevel) level, pos, stack);
        }
        return ItemInteractionResult.CONSUME;
    }

    // Right-click with an empty hand
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) Rituals.describe(serverPlayer);
        return InteractionResult.CONSUME;
    }
}