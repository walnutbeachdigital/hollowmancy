package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VerdantEngineBlockEntity extends BlockEntity {

    public static final int RADIUS = 4;
    public static final int BUFFER = 40;
    public static final int PULSE_INTERVAL = 100;   // every 5 seconds
    public static final int PULSE_COST = 4;
    public static final int PLANTS_PER_PULSE = 6;

    private int mana = 0;
    private int ticks = 0;

    public VerdantEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VERDANT_ENGINE.get(), pos, state);
    }

    public int getMana() {
        return mana;
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

        boolean active = mana >= PULSE_COST;
        setActive(active);
        if (!active || ticks % PULSE_INTERVAL != 0) return;

        if (pulse(serverLevel) > 0) {
            mana -= PULSE_COST;
            setChanged();
            serverLevel.playSound(null, worldPosition, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
    }

    /** Try random spots nearby; grow up to PLANTS_PER_PULSE plants. Returns how many grew. */
    private int pulse(ServerLevel level) {
        RandomSource random = level.random;
        int grown = 0;
        for (int attempt = 0; attempt < 24 && grown < PLANTS_PER_PULSE; attempt++) {
            BlockPos pos = worldPosition.offset(
                    random.nextInt(RADIUS * 2 + 1) - RADIUS, random.nextInt(3) - 1, random.nextInt(RADIUS * 2 + 1) - RADIUS);
            BlockState state = level.getBlockState(pos);
            if (isSpreadingGround(state)) continue; // don't carpet the area in flowers

            if (state.getBlock() instanceof BonemealableBlock plant && plant.isValidBonemealTarget(level, pos, state)) {
                plant.performBonemeal(level, random, pos, state);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        6, 0.3, 0.3, 0.3, 0.0);
                grown++;
            }
        }
        return grown;
    }

    private static boolean isSpreadingGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.CRIMSON_NYLIUM)
                || state.is(Blocks.WARPED_NYLIUM) || state.is(Hollowmancy.HOLLOW_GRASS.get());
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = tag.getInt("mana");
    }
}