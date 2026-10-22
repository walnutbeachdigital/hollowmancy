package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ResearchEngineBlockEntity extends BlockEntity {

    public static final int BUFFER = 50;
    public static final int CYCLE_TICKS = 600;     // 30 seconds per Knowledge
    public static final int SHARDS_PER_CYCLE = 4;
    public static final int MANA_PER_CYCLE = 25;
    public static final int MAX_STORED = 20;

    /** One slot of Dull Shards. Hoppers can fill it (see ModBlockEntities). */
    private final ItemStackHandler shards = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Hollowmancy.DULL_SHARD.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int mana = 0;
    private int knowledge = 0;
    private int progress = 0;
    private int ticks = 0;

    public ResearchEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_ENGINE.get(), pos, state);
    }

    public ItemStackHandler getShardHandler() {
        return shards;
    }

    public int getShardCount() {
        return shards.getStackInSlot(0).getCount();
    }

    public int getKnowledge() {
        return knowledge;
    }

    public boolean isPowered() {
        return mana >= MANA_PER_CYCLE;
    }

    /** Put a held stack of shards in. Whatever doesn't fit stays in the player's hand. */
    public void insertShards(ItemStack held) {
        ItemStack remainder = shards.insertItem(0, held.copy(), false);
        held.setCount(remainder.getCount());
    }

    /** Hand stored Knowledge to the player. Returns how much was given. */
    public int collect(ServerPlayer player) {
        int amount = knowledge;
        if (amount <= 0) return 0;
        knowledge = 0;
        setChanged();
        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data.withKnowledge(data.knowledge() + amount));
        return amount;
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ticks++;

        if (ticks % 20 == 0 && mana < BUFFER / 2) {
            int drawn = ManaNetwork.draw(level, worldPosition, BUFFER - mana);
            if (drawn > 0) {
                mana += drawn;
                setChanged();
            }
        }

        boolean canWork = knowledge < MAX_STORED && getShardCount() >= SHARDS_PER_CYCLE && mana >= MANA_PER_CYCLE;
        setActive(canWork);
        if (!canWork) return; // progress pauses until it can continue

        progress++;
        if (progress % 20 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.5);
        }
        if (progress >= CYCLE_TICKS) {
            progress = 0;
            shards.extractItem(0, SHARDS_PER_CYCLE, false);
            mana -= MANA_PER_CYCLE;
            knowledge++;
            setChanged();
            serverLevel.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);
            serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.6f);
        }
    }

    private void setActive(boolean active) {
        BlockState state = getBlockState();
        if (state.getValue(ManaEngineBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(ManaEngineBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mana", mana);
        tag.putInt("knowledge", knowledge);
        tag.putInt("progress", progress);
        tag.put("shards", shards.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = tag.getInt("mana");
        knowledge = tag.getInt("knowledge");
        progress = tag.getInt("progress");
        if (tag.contains("shards")) shards.deserializeNBT(registries, tag.getCompound("shards"));
    }
}