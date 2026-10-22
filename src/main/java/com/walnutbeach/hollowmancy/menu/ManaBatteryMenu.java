package com.walnutbeach.hollowmancy.menu;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.ManaBatteryBlockEntity;
import com.walnutbeach.hollowmancy.item.ManaVessels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ManaBatteryMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = 2;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final ManaBatteryBlockEntity battery; // only set on the server

    // Client
    public ManaBatteryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(2),
                ContainerLevelAccess.NULL, null);
    }

    // Server
    public ManaBatteryMenu(int containerId, Inventory playerInventory, Container container, ContainerData data,
                           ContainerLevelAccess access, ManaBatteryBlockEntity battery) {
        super(ModMenus.MANA_BATTERY.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 2);
        this.data = data;
        this.access = access;
        this.battery = battery;

        addSlot(new CrystalSlot(container, ManaBatteryBlockEntity.CHARGE_SLOT, 136, 31));
        addSlot(new CrystalSlot(container, ManaBatteryBlockEntity.DRAIN_SLOT, 136, 71));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 172));
        }

        addDataSlots(data); // keeps the screen's mana number in sync
    }

    public int getMana() {
        return data.get(0);
    }

    public int getCapacity() {
        return data.get(1);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (battery == null || !(player instanceof ServerPlayer serverPlayer) || buttonId < 0 || buttonId > 5) return false;
        battery.handleButton(serverPlayer, buttonId);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < MACHINE_SLOTS) {
                if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!ManaVessels.isVessel(stack) || !moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Hollowmancy.MANA_BATTERY.get());
    }

    /** Only Mana Crystals fit. */
    private static class CrystalSlot extends Slot {
        CrystalSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ManaVessels.isVessel(stack);
        }
    }
}