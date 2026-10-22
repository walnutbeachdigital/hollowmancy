package com.walnutbeach.hollowmancy.compat.jei;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.ManaBeaconBlockEntity;
import com.walnutbeach.hollowmancy.familiar.FamiliarType;
import com.walnutbeach.hollowmancy.item.EndgameItems;
import com.walnutbeach.hollowmancy.item.InscribedScrollItem;
import com.walnutbeach.hollowmancy.item.SpellCraftingItems;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import com.walnutbeach.hollowmancy.ritual.Ritual;
import com.walnutbeach.hollowmancy.ritual.Rituals;
import com.walnutbeach.hollowmancy.spell.CraftedSpell;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** JEI pages for the Infuser, the Mana Beacon, familiars and spell scrolls, plus info for this batch's items. */
@JeiPlugin
public class HollowmancyJeiExtrasPlugin implements IModPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "jei_extras");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new InfuserCategory(gui), new BlessingCategory(gui), new FamiliarCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<InfusionRitual> infusions = new ArrayList<>();
        for (Ritual ritual : Rituals.all()) if (ritual instanceof InfusionRitual infusion) infusions.add(infusion);
        registration.addRecipes(InfuserCategory.TYPE, infusions);
        registration.addRecipes(BlessingCategory.TYPE, ManaBeaconBlockEntity.BLESSINGS);
        registration.addRecipes(FamiliarCategory.TYPE, Arrays.asList(FamiliarType.values()));
        registration.addRecipes(RecipeTypes.CRAFTING, scrollRecipes());

        info(registration, Hollowmancy.TORN_PAGE.get(), "Found in wizard towers, and dropped by their wizards. Right-click to study for Knowledge.");
        info(registration, Hollowmancy.TREASURY_KEY.get(), "Dropped by the Castellan at the top of the Revenant Castle's north-east tower. Opens the Vault Door behind Morvane's throne.");
        info(registration, Hollowmancy.TOME_OF_REFLECTION.get(), "Dropped by the Leviathan, kept in the Abyssal Shrine, and found now and then in the Mirror Sea's ruins. Teaches Reflect, which can't be learned by study.");
        info(registration, Hollowmancy.OATHBREAKER.get(), "Dropped by Morvane, the Revenant King. Every hit restores 3 mana.");
        info(registration, EndgameItems.SOVEREIGN_SIGIL.get(), "Dropped by the Hollow Sovereign. One of the three trophies for the Seal of Unmaking.");
        info(registration, EndgameItems.COLOSSUS_HEART.get(), "Dropped by the Amethyst Colossus. One of the three trophies for the Seal of Unmaking.");
        info(registration, EndgameItems.MORVANES_OATH.get(), "Dropped by Morvane, the Revenant King. One of the three trophies for the Seal of Unmaking.");
        info(registration, EndgameItems.SEAL_OF_UNMAKING.get(), "The offering for the Ritual of Unmaking, performed in the Hollow. It calls the Listener, the final boss.");
        info(registration, SpellCraftingItems.BLANK_SCROLL.get(), "Craft a Blank Scroll with one shape rune, one element rune, and (optionally) one modifier rune to inscribe a spell of your own.");
        info(registration, Hollowmancy.PORTCULLIS_ITEM.get(), "Every portcullis block touching another is one gate. Power any of them (a lever beside it, a button, redstone) and the whole gate rises.");
    }

    /** All 36 scroll combinations, shown as ordinary crafting recipes. */
    private static List<RecipeHolder<CraftingRecipe>> scrollRecipes() {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (CraftedSpell.Shape shape : CraftedSpell.Shape.values()) {
            for (CraftedSpell.Element element : CraftedSpell.Element.values()) {
                for (CraftedSpell.Modifier modifier : CraftedSpell.Modifier.values()) {
                    NonNullList<Ingredient> parts = NonNullList.create();
                    parts.add(Ingredient.of(SpellCraftingItems.BLANK_SCROLL.get()));
                    parts.add(Ingredient.of(shape.rune.get()));
                    parts.add(Ingredient.of(element.rune.get()));
                    if (modifier.rune != null) parts.add(Ingredient.of(modifier.rune.get()));
                    String id = CraftedSpell.id(shape, element, modifier);
                    ShapelessRecipe recipe = new ShapelessRecipe("hollowmancy_scroll", CraftingBookCategory.MISC, InscribedScrollItem.of(id), parts);
                    recipes.add(new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "jei/scroll/" + id), recipe));
                }
            }
        }
        return recipes;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Hollowmancy.INFUSER_ITEM.get()), InfuserCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Hollowmancy.MANA_BEACON_ITEM.get()), BlessingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Hollowmancy.RITUAL_ALTAR_ITEM.get()), FamiliarCategory.TYPE);
    }

    private static void info(IRecipeRegistration registration, ItemLike item, String text) {
        registration.addItemStackInfo(new ItemStack(item), Component.literal(text));
    }
}
