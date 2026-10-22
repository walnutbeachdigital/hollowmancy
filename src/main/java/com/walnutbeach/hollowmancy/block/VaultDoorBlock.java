package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** The treasury's door. It can't be broken; only the Treasury Key opens it. */
public class VaultDoorBlock extends Block {

    public VaultDoorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Hollowmancy.TREASURY_KEY.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            // open every vault door block touching this one (the door is two blocks tall)
            Set<BlockPos> seen = new HashSet<>();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            queue.add(pos);
            seen.add(pos);
            while (!queue.isEmpty() && seen.size() < 16) {
                BlockPos door = queue.poll();
                level.levelEvent(2001, door, Block.getId(level.getBlockState(door)));
                level.removeBlock(door, false);
                for (Direction direction : Direction.values()) {
                    BlockPos next = door.relative(direction);
                    if (level.getBlockState(next).getBlock() instanceof VaultDoorBlock && seen.add(next)) queue.add(next);
                }
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.2f, 0.5f);
            level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0f, 0.7f);
            player.displayClientMessage(Component.translatable("hollowmancy.vault_door_block.the_key_turns_the_treasury_of")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("hollowmancy.vault_door_block.locked_fast_the_castellan_keeps_the")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 0.8f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
