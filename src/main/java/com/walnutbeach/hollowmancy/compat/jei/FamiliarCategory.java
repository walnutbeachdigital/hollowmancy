package com.walnutbeach.hollowmancy.compat.jei;

import net.minecraft.network.chat.Component;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.familiar.FamiliarType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Summoning familiars with the Ritual of Binding, and what each one grows into. */
public class FamiliarCategory extends TextCategory<FamiliarType> {

    public static final RecipeType<FamiliarType> TYPE = RecipeType.create(Hollowmancy.MODID, "familiar", FamiliarType.class);

    public FamiliarCategory(IGuiHelper gui) {
        super(TYPE, "Familiars", gui.createDrawableItemStack(new ItemStack(Items.FEATHER)), 112);
    }

    private static String[] levels(FamiliarType type) {
        return switch (type) {
            case OWL -> new String[] {"Night Vision", "Monsters within 16 blocks glow", "...within 20 blocks",
                "Catches you when you fall; 24 blocks", "Monsters glow within 28 blocks"};
            case WISP -> new String[] {"+1 mana every 2 seconds", "+1 mana every 2 seconds", "+2 mana every 2 seconds",
                "+2 mana; washes away corruption", "+3 mana every 2 seconds"};
            case GOLEM -> new String[] {"Fights for you (4 damage)", "6 damage", "7 damage; Resistance while near",
                "9 damage", "10 damage; its blows slow enemies"};
            case MOTH -> new String[] {"Leaves light behind you in the dark (30s); a flower every 60s", "Light lasts 40s; a flower every 50s",
                "Light lasts 50s; a flower every 40s", "Light lasts 60s; a flower every 30s", "Light lasts 70s; a flower every 20s; slows your falls"};
            case GLIMMERHORN -> new String[] {"Ride it: 35% faster than sprinting", "50% faster", "65% faster; steps up 1.5 blocks",
                "80% faster; Night Vision while riding", "95% faster"};
        };
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FamiliarType recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 14).setStandardSlotBackground().addItemStack(new ItemStack(recipe.offering().get()));
    }

    @Override
    public void draw(FamiliarType recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(font(), Component.translatable("hollowmancy.familiar_category.the", recipe.displayName()), 2, 2, 0xFF6A3D9A, false);
        text(graphics, "Ritual of Binding: offer this. Candles 2 out on each side, Amethyst Blocks 2 out on each diagonal. "
            + (recipe == FamiliarType.MOTH || recipe == FamiliarType.GLIMMERHORN ? "Only in the Hollow. Ascension, 120 mana." : "Apprentice, 50 mana."), 28, 14, 0xFF777777);
        int y = 46;
        String[] lines = levels(recipe);
        int[] bond = {0, 10, 30, 60, 100};
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(font(), "L" + (i + 1), 2, y, 0xFFB08A2A, false);
            graphics.drawString(font(), String.valueOf(bond[i]), 16, y, 0xFF999999, false);
            y = text(graphics, lines[i], 34, y, 0xFF404040);
        }
        graphics.drawString(font(), Component.translatable("hollowmancy.familiar_category.bond_1_a_minute_together_2"), 2, 102, 0xFF999999, false);
    }
}
