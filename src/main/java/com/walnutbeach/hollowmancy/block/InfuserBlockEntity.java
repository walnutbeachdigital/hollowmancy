package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import com.walnutbeach.hollowmancy.ritual.Ritual;
import com.walnutbeach.hollowmancy.ritual.Rituals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class InfuserBlockEntity extends BlockEntity {

    public static final int WORK_TICKS = 200;   // 10 seconds per infusion

    /** The offering slot. Only accepts things some Infusion uses. */
    private final ItemStackHandler input = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return findInfusion(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** The finished slot. Nothing can be put in from outside. */
    private final ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int mana = 0;
    private int progress = 0;
    private int attunedStage = 0;
    private int ticks = 0;
    private int lastSync = 0;
    /** Client only: the progress the display shows, easing toward the real value. */
    public float shownProgress = -1;

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFUSER.get(), pos, state);
    }

    /** The Infusion (not Fusion: that needs a second hand) that takes this item, or null. */
    public static InfusionRitual findInfusion(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Ritual ritual : Rituals.all()) {
            if (ritual instanceof InfusionRitual infusion && stack.is(infusion.getOffering())) return infusion;
        }
        return null;
    }

    /** Hoppers: from below they take finished items, from anywhere else they add offerings. */
    public IItemHandler getHandler(Direction side) {
        return side == Direction.DOWN ? output : input;
    }

    public ItemStackHandler getInput() { return input; }
    public ItemStackHandler getOutput() { return output; }
    public int getMana() { return mana; }
    public int getProgressPercent() { return progress * 100 / WORK_TICKS; }
    public int getAttunedStage() { return attunedStage; }

    public void attune(int stage) {
        attunedStage = stage;
        setChanged();
    }

    private boolean hasRoomFor(ItemStack result) {
        ItemStack current = output.getStackInSlot(0);
        if (current.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(current, result) && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    /** Why it isn't working, for the status message. Null when it's fine. */
    public String problem() {
        InfusionRitual recipe = findInfusion(input.getStackInSlot(0));
        if (recipe == null) return "idle. Give it an Infusion offering.";
        if (attunedStage < recipe.getRequiredStage()) return "this infusion is beyond whoever attuned it.";
        if (!hasRoomFor(new ItemStack(recipe.getResult()))) return "full. Take the finished item.";
        if (mana < recipe.getManaCost()) return "waiting for mana (" + mana + "/" + recipe.getManaCost() + ").";
        return null;
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ticks++;
        int signature = java.util.Objects.hash(progress, mana, attunedStage, input.getStackInSlot(0).getItem(),
                input.getStackInSlot(0).getCount(), output.getStackInSlot(0).getCount());
        if (ticks % 4 == 0 && signature != lastSync) {
            lastSync = signature;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }

        InfusionRitual recipe = findInfusion(input.getStackInSlot(0));
        boolean allowed = recipe != null && attunedStage >= recipe.getRequiredStage();
        ItemStack result = allowed ? new ItemStack(recipe.getResult()) : ItemStack.EMPTY;
        boolean room = allowed && hasRoomFor(result);

        if (room && mana < recipe.getManaCost() && ticks % 20 == 0) {
            int drawn = ManaNetwork.draw(level, worldPosition, recipe.getManaCost() - mana);
            if (drawn > 0) {
                mana += drawn;
                setChanged();
            }
        }

        boolean working = room && mana >= recipe.getManaCost();
        setActive(working);
        if (!working) {
            if (recipe == null && progress != 0) progress = 0;
            return;
        }

        progress++;
        if (progress % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 1.4,
                worldPosition.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.6);
        }
        if (progress < WORK_TICKS) return;

        // Done: spend everything, make the item
        progress = 0;
        mana -= recipe.getManaCost();
        input.extractItem(0, 1, false);
        ItemStack current = output.getStackInSlot(0);
        if (current.isEmpty()) output.setStackInSlot(0, result);
        else current.grow(result.getCount());
        setChanged();

        serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2,
            worldPosition.getZ() + 0.5, 25, 0.25, 0.4, 0.25, 0.08);
        serverLevel.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 0.8f);
    }

    private void setActive(boolean active) {
        BlockState state = getBlockState();
        if (state.getValue(ManaEngineBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(ManaEngineBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    /** Nearby players get the Infuser's contents and progress, for the floating display. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input", input.serializeNBT(registries));
        tag.put("output", output.serializeNBT(registries));
        tag.putInt("mana", mana);
        tag.putInt("progress", progress);
        tag.putInt("attuned_stage", attunedStage);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        input.deserializeNBT(registries, tag.getCompound("input"));
        output.deserializeNBT(registries, tag.getCompound("output"));
        mana = tag.getInt("mana");
        progress = tag.getInt("progress");
        attunedStage = tag.getInt("attuned_stage");
    }
}
