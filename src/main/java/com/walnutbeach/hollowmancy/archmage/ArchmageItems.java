package com.walnutbeach.hollowmancy.archmage;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** What there is to reach for after Archmage. */
public final class ArchmageItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    /** Granted once you become an Archmage, and dropped by every Echo you silence. Offer it to call the Echo. */
    public static final DeferredItem<Item> SILENT_SEAL = ITEMS.register("silent_seal",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    /** Dropped by the Echo of the Listener. Infuse it into the Archmage's Staff. */
    public static final DeferredItem<Item> ECHO_HEART = ITEMS.register("echo_heart",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<ArchmageStaffItem> ARCHMAGE_STAFF = ITEMS.register("archmage_staff",
            () -> new ArchmageStaffItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addToTab(CreativeModeTab.Output output) {
        for (DeferredItem<? extends Item> item : List.of(SILENT_SEAL, ECHO_HEART, ARCHMAGE_STAFF)) output.accept(item.get());
    }

    private ArchmageItems() {}
}
