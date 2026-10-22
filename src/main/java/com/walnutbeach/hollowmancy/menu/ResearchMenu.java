package com.walnutbeach.hollowmancy.menu;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ResearchMenu extends AbstractContainerMenu {

    public static final int SHARD_COST = 4;
    public static final int MANA_COST = 50;
    public static final int STUDY_BUTTON = 0;

    private final Container container = new SimpleContainer(1);
    private final ContainerLevelAccess access;

    // Used on the client
    public ResearchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Used on the server
    public ResearchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.RESEARCH_TABLE.get(), containerId);
        this.access = access;

        // Shard slot (only accepts Dull Shards)
        addSlot(new Slot(container, 0, 26, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Hollowmancy.DULL_SHARD.get());
            }
        });

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public static boolean hasEnoughShards(ItemStack stack) {
        return stack.is(Hollowmancy.DULL_SHARD.get()) && stack.getCount() >= SHARD_COST;
    }

    // Called on the server when the Study button is clicked
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != STUDY_BUTTON || !(player instanceof ServerPlayer serverPlayer)) return false;

        ItemStack shards = container.getItem(0);
        MagicData data = MagicHelper.get(serverPlayer);
        if (!hasEnoughShards(shards) || data.mana() < MANA_COST) return false;

        shards.shrink(SHARD_COST);
        container.setChanged();

        MagicData updated = data.withMana(data.mana() - MANA_COST).withKnowledge(data.knowledge() + 2);

        if (updated.stage() < MagicStage.APPRENTICE) {
            updated = updated.withStage(MagicStage.APPRENTICE);
            serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.research_menu.the_markings_shift_and_rearrange_and")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.8f);
        } else {
            serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        MagicHelper.set(serverPlayer, updated);
        return true;
    }

    // Shift-click handling
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                // Shard slot -> player inventory
                if (!moveItemStackTo(stack, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                // Player inventory -> shard slot (shards only)
                if (!stack.is(Hollowmancy.DULL_SHARD.get()) || !moveItemStackTo(stack, 0, 1, false)) {
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
        return stillValid(access, player, Hollowmancy.RESEARCH_TABLE.get());
    }

    // Give shards back when the screen closes
    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, container));
    }
}