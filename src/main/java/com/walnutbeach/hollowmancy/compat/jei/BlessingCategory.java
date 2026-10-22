package com.walnutbeach.hollowmancy.compat.jei;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.ManaBeaconBlockEntity;
import com.walnutbeach.hollowmancy.block.ManaBeaconBlockEntity.Blessing;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** The Mana Beacon's blessings, and the item that tunes each one. */
public class BlessingCategory extends TextCategory<Blessing> {

    public static final RecipeType<Blessing> TYPE = RecipeType.create(Hollowmancy.MODID, "beacon_blessing", Blessing.class);

    public BlessingCategory(IGuiHelper gui) {
        super(TYPE, "Mana Beacon", gui.createDrawableItemStack(new ItemStack(Hollowmancy.MANA_BEACON_ITEM.get())), 56);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Blessing recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 14).setStandardSlotBackground().addItemStack(new ItemStack(recipe.key()));
    }

    @Override
    public void draw(Blessing recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(font(), recipe.name(), 2, 2, 0xFF6A3D9A, false);
        String what = recipe.effect() == null
            ? "Pours mana into every awakened mage nearby: 2 mana every 4 seconds, on top of their own regeneration."
            : "Grants " + recipe.effect().value().getDisplayName().getString() + " to every player nearby.";
        text(graphics, what, 28, 14, 0xFF404040);
        text(graphics, "Right-click the beacon with this item to tune it (it isn't used up). Range " + ManaBeaconBlockEntity.RADIUS
            + " blocks. Costs " + ManaBeaconBlockEntity.COST_PER_PLAYER + " mana per player every 4 seconds.", 2, 36, 0xFF777777);
    }
}
