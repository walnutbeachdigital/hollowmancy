package com.walnutbeach.hollowmancy.item.hollow;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** What the Hollow's creatures leave behind, and what you make from it. */
public final class HollowMaterials {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    // ---- Dropped by creatures ----
    public static final DeferredItem<Item> WISP_ESSENCE = ITEMS.register("wisp_essence", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> MOTH_DUST = ITEMS.register("moth_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ANTLER_SHARD = ITEMS.register("antler_shard", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHADE_CLOTH = ITEMS.register("shade_cloth", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LURKER_SCALE = ITEMS.register("lurker_scale", () -> new Item(new Item.Properties()));

    // ---- Charms (infused at the Ritual Altar or the Infuser; work from anywhere in your inventory) ----
    public static final DeferredItem<HollowCharmItem> WISPBOUND_CHARM = charm("wispbound_charm", HollowCharmItem.Kind.WISPBOUND);
    public static final DeferredItem<HollowCharmItem> MOTHWING_CHARM = charm("mothwing_charm", HollowCharmItem.Kind.MOTHWING);
    public static final DeferredItem<HollowCharmItem> HUNTSMAN_CHARM = charm("huntsman_charm", HollowCharmItem.Kind.HUNTSMAN);
    public static final DeferredItem<HollowCharmItem> SHADEWEAVE_CHARM = charm("shadeweave_charm", HollowCharmItem.Kind.SHADEWEAVE);
    public static final DeferredItem<HollowCharmItem> TIDEBOUND_CHARM = charm("tidebound_charm", HollowCharmItem.Kind.TIDEBOUND);

    // ---- Crafted ----
    public static final DeferredItem<ManaTonicItem> MANA_TONIC = ITEMS.register("mana_tonic",
            () -> new ManaTonicItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    private static DeferredItem<HollowCharmItem> charm(String name, HollowCharmItem.Kind kind) {
        return ITEMS.register(name, () -> new HollowCharmItem(kind, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    }

    private static final List<DeferredItem<? extends Item>> ALL = List.of(WISP_ESSENCE, MOTH_DUST, ANTLER_SHARD, SHADE_CLOTH, LURKER_SCALE,
            WISPBOUND_CHARM, MOTHWING_CHARM, HUNTSMAN_CHARM, SHADEWEAVE_CHARM, TIDEBOUND_CHARM, MANA_TONIC);

    /** Called from Hollowmancy's constructor. */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    /** Called from Hollowmancy's creative tab. */
    public static void addToTab(CreativeModeTab.Output output) {
        ALL.forEach(item -> output.accept(item.get()));
    }

    private HollowMaterials() {}
}
