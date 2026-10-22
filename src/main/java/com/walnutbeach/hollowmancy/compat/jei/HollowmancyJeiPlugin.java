package com.walnutbeach.hollowmancy.compat.jei;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.ritual.Rituals;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

@JeiPlugin
public class HollowmancyJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RitualCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Every ritual and infusion, built from the ritual registry
        List<RitualDisplay> rituals = Rituals.all().stream().map(RitualDisplay::from).toList();
        registration.addRecipes(RitualCategory.TYPE, rituals);

        // Information pages for things that have no recipe
        info(registration, Hollowmancy.GLIMMERSTONE_ORE_ITEM.get(),
                "Found underground between Y 0 and 64 in every Overworld biome. Needs a stone pickaxe or better.");
        info(registration, Hollowmancy.DULL_SHARD.get(),
                "Mined from Glimmerstone Ore. Hold one beneath an open, stormy sky to awaken to magic.");
        info(registration, Hollowmancy.VOIDSTONE_ORE_ITEM.get(),
                "Found only in the floating islands of the Hollow. Needs a diamond pickaxe. Mine it with Silk Touch to keep the ore for rituals.");
        info(registration, Hollowmancy.VOID_SHARD.get(),
                "Mined from Voidstone Ore in the Hollow. Also found in the chests of its ruined towers.");
        info(registration, Hollowmancy.LOST_PAGE.get(),
                "Found in the chests of the Hollow's ruined towers. Right-click to read: each page is copied into your Arcane Journal and grants 5 Knowledge.");
        info(registration, Hollowmancy.SOVEREIGN_CROWN.get(),
                "Dropped by the Hollow Sovereign, who is woken by the Ritual of Waking.");
        info(registration, Hollowmancy.RESEARCH_TABLE_ITEM.get(),
                "Right-click once awakened. Study with 4 Dull Shards and 50 mana to gain Knowledge.");
        info(registration, Hollowmancy.RITUAL_ALTAR_ITEM.get(),
                "Build a ritual's pattern around the altar, at the altar's height, then right-click it while holding the offering. See the Rituals category.");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Hollowmancy.RITUAL_ALTAR_ITEM.get()), RitualCategory.TYPE);
    }

    private static void info(IRecipeRegistration registration, ItemLike item, String text) {
        registration.addItemStackInfo(new ItemStack(item), Component.literal(text));
    }
}