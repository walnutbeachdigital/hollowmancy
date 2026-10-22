package com.walnutbeach.hollowmancy.villager;

import com.walnutbeach.hollowmancy.Hollowmancy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class ArcanistTrades {

    private static final float PRICE_MULTIPLIER = 0.05f; // how much prices swing with demand (vanilla standard)

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.ARCANIST.get()) return;
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        // ---- 1: Novice ----
        trades.get(1).add(trade(Hollowmancy.DULL_SHARD.get(), 6, () -> new ItemStack(Items.EMERALD), 16, 2));
        trades.get(1).add(trade(Hollowmancy.DULL_SHARD.get(), 4, () -> new ItemStack(Items.CANDLE, 3), 12, 2));
        trades.get(1).add(trade(Hollowmancy.DULL_SHARD.get(), 3, () -> new ItemStack(Items.BOOK), 12, 2));

        // ---- 2: Apprentice ----
        trades.get(2).add(trade(Hollowmancy.DULL_SHARD.get(), 6, () -> new ItemStack(Items.AMETHYST_SHARD, 4), 12, 5));
        trades.get(2).add(trade(Items.EMERALD, 1, Hollowmancy.DULL_SHARD.get(), 4, () -> new ItemStack(Items.EXPERIENCE_BOTTLE, 3), 12, 5));
        trades.get(2).add(trade(Items.EMERALD, 3, () -> PatchouliAPI.get().getBookStack(
                ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcane_journal")), 4, 5));

        // ---- 3: Journeyman ----
        trades.get(3).add(trade(Hollowmancy.DULL_SHARD.get(), 8, () -> new ItemStack(Items.LAPIS_BLOCK), 12, 10));
        trades.get(3).add(trade(Items.EMERALD, 2, () -> new ItemStack(Items.PURPLE_WOOL, 8), 12, 10));
        trades.get(3).add(trade(Items.EMERALD, 6, () -> new ItemStack(Hollowmancy.ARCANIST_HOOD.get()), 3, 15));

        // ---- 4: Expert ----
        trades.get(4).add(trade(Items.EMERALD, 10, Hollowmancy.DULL_SHARD.get(), 6, () -> new ItemStack(Hollowmancy.ARCANIST_ROBE.get()), 3, 20));
        trades.get(4).add(trade(Items.EMERALD, 8, () -> new ItemStack(Hollowmancy.ARCANIST_BOOTS.get()), 3, 20));
        trades.get(4).add(trade(Hollowmancy.DULL_SHARD.get(), 16, () -> new ItemStack(Items.CRYING_OBSIDIAN, 2), 8, 20));

        // ---- 5: Master ----
        trades.get(5).add(trade(Items.EMERALD, 20, () -> new ItemStack(Items.ECHO_SHARD), 2, 30));
        trades.get(5).add(trade(Items.EMERALD, 12, () -> new ItemStack(Items.GOLDEN_APPLE, 2), 4, 30));
        trades.get(5).add(trade(Items.EMERALD, 14, Hollowmancy.DULL_SHARD.get(), 4, () -> new ItemStack(Hollowmancy.ARCANIST_LEGGINGS.get()), 3, 30));
    }

    /** One cost -> one result. */
    private static VillagerTrades.ItemListing trade(ItemLike cost, int costCount, Supplier<ItemStack> result, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(cost, costCount), result.get(), maxUses, xp, PRICE_MULTIPLIER);
    }

    /** Two costs -> one result. */
    private static VillagerTrades.ItemListing trade(ItemLike costA, int countA, ItemLike costB, int countB,
                                                    Supplier<ItemStack> result, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(costA, countA), Optional.of(new ItemCost(costB, countB)),
                result.get(), maxUses, xp, PRICE_MULTIPLIER);
    }
}