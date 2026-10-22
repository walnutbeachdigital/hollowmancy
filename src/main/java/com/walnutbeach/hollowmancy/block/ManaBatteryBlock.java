package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.menu.ManaBatteryMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ManaBatteryBlock extends Block implements EntityBlock {

    /** 0 = empty ... 4 = full. Picks which glass texture is shown. */
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 4);
    public static final int QUICK_TRANSFER = 50;
    private static final Component TITLE = Component.translatable("container.hollowmancy.mana_battery");

    public ManaBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaBatteryBlockEntity(pos, state);
    }

    // Server-side ticking: charges and drains crystals in the slots
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.MANA_BATTERY.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((ManaBatteryBlockEntity) blockEntity).serverTick();
    }

    // Right-click: open the screen. Sneak + right-click: quickly store 50.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ManaBatteryBlockEntity battery)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        if (!MagicHelper.hasStage(serverPlayer, MagicStage.AWAKENING)) {
            serverPlayer.displayClientMessage(Component.translatable("hollowmancy.mana_battery_block.a_cold_humming_vessel_you_feel")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            MagicData data = MagicHelper.get(serverPlayer);
            int moved = battery.insert(Math.min(QUICK_TRANSFER, data.mana()));
            if (moved > 0) {
                MagicHelper.set(serverPlayer, data.withMana(data.mana() - moved));
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.6f);
            }
            serverPlayer.displayClientMessage(Component.translatable("hollowmancy.mana_battery_block.mana_battery", battery.getMana(), ManaBatteryBlockEntity.CAPACITY)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResult.CONSUME;
        }

        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, p) -> new ManaBatteryMenu(containerId, inventory,
                        battery.getItems(), battery.dataAccess(), ContainerLevelAccess.create(level, pos), battery),
                TITLE));
        return InteractionResult.CONSUME;
    }

    // Keep the crystals when the battery is broken (only when it's really removed, not when its glass level changes)
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ManaBatteryBlockEntity battery) {
            Containers.dropContents(level, pos, battery.getItems());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ManaBatteryBlockEntity battery ? battery.comparatorSignal() : 0;
    }
}