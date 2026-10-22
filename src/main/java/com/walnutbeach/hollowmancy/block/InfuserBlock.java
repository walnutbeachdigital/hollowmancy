package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InfuserBlock extends ManaEngineBlock {

    private static final String[] STAGE_NAMES = {"", "the Mundane", "the Awakening", "Apprentice", "the Arcane Arts", "Mastery", "Ascension"};

    public InfuserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfuserBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.INFUSER.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((InfuserBlockEntity) blockEntity).serverTick();
    }

    // Attuned to whoever places it
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof InfuserBlockEntity infuser) {
            infuser.attune(MagicHelper.get(player).stage());
        }
    }

    // Right-click with an offering: put it in
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (InfuserBlockEntity.findInfusion(stack) == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof InfuserBlockEntity infuser) {
            ItemStack left = infuser.getInput().insertItem(0, stack.copy(), false);
            if (left.getCount() == stack.getCount()) {
                player.displayClientMessage(Component.translatable("hollowmancy.infuser_block.the_infuser_is_already_holding_something")
                    .withStyle(ChatFormatting.GRAY), true);
            } else {
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, left);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // Empty hand: take the finished item, or see what it's doing. Sneak: attune it to yourself.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof InfuserBlockEntity infuser) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            int stage = MagicHelper.get(serverPlayer).stage();
            infuser.attune(stage);
            player.displayClientMessage(Component.translatable("hollowmancy.infuser_block.the_infuser_is_attuned_to_you", stageName(stage))
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResult.CONSUME;
        }

        ItemStack finished = infuser.getOutput().extractItem(0, 64, false);
        if (!finished.isEmpty()) {
            if (!player.getInventory().add(finished)) player.drop(finished, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.CONSUME;
        }

        String problem = infuser.problem();
        Component message;
        if (problem == null) {
            InfusionRitual recipe = InfuserBlockEntity.findInfusion(infuser.getInput().getStackInSlot(0));
            message = Component.translatable("hollowmancy.infuser_block.infuser_making", new ItemStack(recipe.getResult()).getHoverName().getString(), infuser.getProgressPercent()).withStyle(ChatFormatting.LIGHT_PURPLE);
        } else {
            message = Component.translatable("hollowmancy.infuser_block.infuser", problem).withStyle(ChatFormatting.GRAY);
        }
        player.displayClientMessage(message, true);
        return InteractionResult.CONSUME;
    }

    private static String stageName(int stage) {
        return stage >= 1 && stage < STAGE_NAMES.length ? STAGE_NAMES[stage] : "stage " + stage;
    }

    // Breaking it drops whatever is inside
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof InfuserBlockEntity infuser) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), infuser.getInput().getStackInSlot(0));
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), infuser.getOutput().getStackInSlot(0));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
