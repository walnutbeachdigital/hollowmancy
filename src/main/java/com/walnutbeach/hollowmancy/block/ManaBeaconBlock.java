package com.walnutbeach.hollowmancy.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ManaBeaconBlock extends ManaEngineBlock {

    public ManaBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaBeaconBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.MANA_BEACON.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((ManaBeaconBlockEntity) blockEntity).serverTick();
    }

    // Right-click with a tuning item: choose the blessing (the item is not used up)
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        ManaBeaconBlockEntity.Blessing blessing = ManaBeaconBlockEntity.forItem(stack);
        if (blessing == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ManaBeaconBlockEntity beacon) {
            beacon.setBlessing(blessing);
            level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.3f);
            player.displayClientMessage(Component.translatable("hollowmancy.mana_beacon_block.the_beacon_now_grants", blessing.name())
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // Empty hand: what is it doing?
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ManaBeaconBlockEntity beacon)) return InteractionResult.CONSUME;
        ManaBeaconBlockEntity.Blessing blessing = beacon.getBlessing();
        Component message;
        if (blessing == null) {
            message = Component.translatable("hollowmancy.mana_beacon_block.mana_beacon_no_blessing_chosen_right").withStyle(ChatFormatting.GRAY);
        } else if (state.getValue(ACTIVE)) {
            message = Component.translatable("hollowmancy.mana_beacon_block.mana_beacon_for_everyone_within_blocks", blessing.name(), ManaBeaconBlockEntity.RADIUS, beacon.getMana()).withStyle(ChatFormatting.LIGHT_PURPLE);
        } else {
            message = Component.translatable("hollowmancy.mana_beacon_block.mana_beacon_tuned_to_but_idle", blessing.name()).withStyle(ChatFormatting.GRAY);
        }
        player.displayClientMessage(message, true);
        return InteractionResult.CONSUME;
    }
}
