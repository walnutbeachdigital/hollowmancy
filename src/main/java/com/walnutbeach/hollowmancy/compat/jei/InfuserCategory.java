package com.walnutbeach.hollowmancy.compat.jei;

import net.minecraft.network.chat.Component;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.InfuserBlockEntity;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Every Infusion, as the Infuser performs it. */
public class InfuserCategory extends TextCategory<InfusionRitual> {

    public static final RecipeType<InfusionRitual> TYPE = RecipeType.create(Hollowmancy.MODID, "infuser", InfusionRitual.class);

    public InfuserCategory(IGuiHelper gui) {
        super(TYPE, "Infuser", gui.createDrawableItemStack(new ItemStack(Hollowmancy.INFUSER_ITEM.get())), 58);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRitual recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 14).setStandardSlotBackground().addItemStack(new ItemStack(recipe.getOffering()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 50, 14).setStandardSlotBackground().addItemStack(new ItemStack(recipe.getResult()));
    }

    @Override
    public void draw(InfusionRitual recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(font(), recipe.getName().replace("Infusion: ", ""), 2, 2, 0xFF6A3D9A, false);
        graphics.drawString(font(), "→", 30, 19, 0xFF9B5DE5, false);
        graphics.drawString(font(), Component.translatable("jade.hollowmancy.mana", recipe.getManaCost()), 76, 14, 0xFF9B5DE5, false);
        graphics.drawString(font(), Component.translatable("hollowmancy.infuser_category.seconds", (InfuserBlockEntity.WORK_TICKS / 20)), 76, 24, 0xFF555555, false);
        text(graphics, "Attuned to " + MagicStage.name(recipe.getRequiredStage()) + " or higher. No pattern needed.", 2, 38, 0xFF404040);
    }
}
