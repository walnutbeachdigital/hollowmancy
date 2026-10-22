package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ResearchEngineBlock extends ManaEngineBlock {

    public ResearchEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchEngineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.RESEARCH_ENGINE.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((ResearchEngineBlockEntity) blockEntity).serverTick();
    }

    // Right-click holding Dull Shards: feed them in
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Hollowmancy.DULL_SHARD.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof ResearchEngineBlockEntity engine) {
            engine.insertShards(stack);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.8f, 1.2f);
            player.displayClientMessage(Component.translatable("hollowmancy.research_engine_block.research_engine_shards", engine.getShardCount())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    // Empty hand: collect Knowledge, or show status
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ResearchEngineBlockEntity engine)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        if (engine.getKnowledge() > 0 && MagicHelper.hasStage(serverPlayer, MagicStage.AWAKENING)) {
            int amount = engine.collect(serverPlayer);
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6f, 1.4f);
            serverPlayer.displayClientMessage(Component.translatable("hollowmancy.research_engine_block.you_take_in_what_the_engine", amount)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResult.CONSUME;
        }

        String status = state.getValue(ACTIVE) ? "studying"
                : !engine.isPowered() ? "no power, connect a charged Mana Battery"
                : engine.getShardCount() < ResearchEngineBlockEntity.SHARDS_PER_CYCLE ? "needs Dull Shards"
                : "full, collect its Knowledge";
        serverPlayer.displayClientMessage(Component.translatable("hollowmancy.research_engine_block.research_engine_shards_knowledge_ready", engine.getShardCount(), engine.getKnowledge(), status).withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }

    // Drop the shards inside when broken (not when only the glow changes)
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ResearchEngineBlockEntity engine) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), engine.getShardHandler().getStackInSlot(0));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}