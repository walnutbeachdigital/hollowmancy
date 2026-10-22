package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.ManaCrystalItem;
import com.walnutbeach.hollowmancy.item.ManaVessels;
import com.walnutbeach.hollowmancy.item.ModComponents;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.walnutbeach.hollowmancy.magic.LeyLines;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class ManaBatteryBlockEntity extends BlockEntity {

    public static final int CAPACITY = 2000;
    public static final int CRYSTAL_RATE = 5; // mana per tick into or out of a crystal
    public static final int CHARGE_SLOT = 0;
    public static final int DRAIN_SLOT = 1;

    private int mana = 0;

    /** The two crystal slots. Saved with the block. */
    private final SimpleContainer items = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ManaBatteryBlockEntity.this.setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return ManaVessels.isVessel(stack);
        }
    };

    /** What the open screen sees: [0] = stored mana, [1] = capacity. */
    private final ContainerData syncedData = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? mana : CAPACITY;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) mana = value;
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ManaBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_BATTERY.get(), pos, state);
    }

    public int getMana() {
        return mana;
    }

    public int space() {
        return CAPACITY - mana;
    }

    public SimpleContainer getItems() {
        return items;
    }

    public ContainerData dataAccess() {
        return syncedData;
    }

    /** Add up to `amount`. Returns how much was actually added. */
    public int insert(int amount) {
        int added = Math.min(amount, space());
        if (added > 0) {
            mana += added;
            changed();
        }
        return added;
    }

    /** Take up to `amount`. Returns how much was actually taken. */
    public int extract(int amount) {
        int taken = Math.min(amount, mana);
        if (taken > 0) {
            mana -= taken;
            changed();
        }
        return taken;
    }

    public int comparatorSignal() {
        return mana == 0 ? 0 : Math.max(1, Math.round(15.0f * mana / CAPACITY));
    }

    /** Screen buttons: 0-2 store 10 / 50 / all, 3-5 draw 10 / 50 / all. */
    public void handleButton(ServerPlayer player, int buttonId) {
        MagicData data = MagicHelper.get(player);
        int amount = switch (buttonId % 3) {
            case 0 -> 10;
            case 1 -> 50;
            default -> Integer.MAX_VALUE;
        };

        if (buttonId < 3) {
            int moved = insert(Math.min(amount, data.mana()));
            if (moved > 0) {
                MagicHelper.set(player, data.withMana(data.mana() - moved));
                playChime(0.6f);
            }
        } else {
            int moved = extract(Math.min(amount, data.maxMana() - data.mana()));
            if (moved > 0) {
                MagicHelper.set(player, data.withMana(data.mana() + moved));
                playChime(1.4f);
            }
        }
    }

    private void playChime(float pitch) {
        if (level != null) level.playSound(null, worldPosition, pitch < 1.0f ? ModSounds.BATTERY_CHARGE.get() : ModSounds.BATTERY_DRAIN.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
    }

    public void serverTick() {
        if (syncPending && level != null && level.getGameTime() % 4 == 0) {
            syncPending = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        LeyLines.chargeBattery(level, worldPosition, this);
        ItemStack charging = items.getItem(CHARGE_SLOT);
        if (ManaVessels.isVessel(charging) && mana > 0) {
            int stored = ManaVessels.stored(charging);
            int move = Math.min(CRYSTAL_RATE, Math.min(mana, ManaVessels.capacity(charging) - stored));
            if (move > 0) {
                ManaVessels.setStored(charging, stored + move);
                extract(move);
                items.setChanged();
            }
        }

        ItemStack draining = items.getItem(DRAIN_SLOT);
        if (ManaVessels.isVessel(draining)) {
            int stored = ManaVessels.stored(draining);
            int move = Math.min(CRYSTAL_RATE, Math.min(stored, space()));
            if (move > 0) {
                ManaVessels.setStored(draining, stored - move);
                insert(move);
                items.setChanged();
            }
        }
    }

    /** Save, update the glass fill level, and tell comparators. */
    private void changed() {
        setChanged();
        syncPending = true;
        if (level == null || level.isClientSide) return;

        int charge = mana == 0 ? 0 : Math.max(1, Math.min(4, (int) Math.ceil(4.0 * mana / CAPACITY)));
        BlockState state = getBlockState();
        if (state.hasProperty(ManaBatteryBlock.CHARGE) && state.getValue(ManaBatteryBlock.CHARGE) != charge) {
            level.setBlock(worldPosition, state.setValue(ManaBatteryBlock.CHARGE, charge), Block.UPDATE_ALL);
        }
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
    }

    /** Hollowmancy: the mana amount is sent to nearby players a few times a second, for the live column and number. */
    private boolean syncPending = true;
    /** Client only: the level the renderer shows, easing toward the real amount. */
    public float shownMana = -1;

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("mana", mana);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mana", mana);
        ContainerHelper.saveAllItems(tag, items.getItems(), registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = tag.getInt("mana");
        NonNullList<ItemStack> loaded = NonNullList.withSize(items.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, loaded, registries);
        for (int i = 0; i < loaded.size(); i++) items.setItem(i, loaded.get(i));
    }
}