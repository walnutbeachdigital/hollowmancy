package com.walnutbeach.hollowmancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WardingLanternBlockEntity extends BlockEntity {

    public static final int BUFFER = 100;          // mana the lantern holds itself
    public static final int REFILL_BELOW = 50;     // tops up from the network when below this
    public static final int UPKEEP_INTERVAL = 60;  // spends 1 mana every 3 seconds

    private int mana = 0;
    private int ticks = 0;

    public WardingLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDING_LANTERN.get(), pos, state);
    }

    public int getMana() {
        return mana;
    }

    /** Runs every server tick. */
    public void serverTick() {
        if (level == null) return;
        ticks++;

        if (ticks % 20 == 0 && mana < REFILL_BELOW) {
            int drawn = ManaNetwork.draw(level, worldPosition, BUFFER - mana);
            if (drawn > 0) {
                mana += drawn;
                setChanged();
            }
        }

        if (ticks % UPKEEP_INTERVAL == 0 && mana > 0) {
            mana--;
            setChanged();
        }

        boolean lit = mana > 0;
        BlockState state = getBlockState();
        if (state.getValue(WardingLanternBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(WardingLanternBlock.LIT, lit), Block.UPDATE_ALL);
        }
        WardingLanterns.setLit(level, worldPosition, lit);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) WardingLanterns.setLit(level, worldPosition, false);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null) WardingLanterns.setLit(level, worldPosition, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mana", mana);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = tag.getInt("mana");
    }
}