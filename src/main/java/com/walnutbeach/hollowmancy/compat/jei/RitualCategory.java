package com.walnutbeach.hollowmancy.compat.jei;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class RitualCategory implements IRecipeCategory<RitualDisplay> {

    public static final RecipeType<RitualDisplay> TYPE = RecipeType.create(Hollowmancy.MODID, "ritual", RitualDisplay.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 124;
    private static final int GRID_X = 2;
    private static final int GRID_Y = 14;
    private static final int GRID_SIZE = 66;
    private static final int RIGHT_X = 78;

    private final IDrawable icon;

    public RitualCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Hollowmancy.RITUAL_ALTAR_ITEM.get()));
    }

    @Override
    public RecipeType<RitualDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("hollowmancy.ritual_category.rituals");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RitualDisplay recipe, IFocusGroup focuses) {
        // Offering -> result
        builder.addSlot(RecipeIngredientRole.INPUT, RIGHT_X, GRID_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.ritual().getOffering()));
        if (!recipe.result().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RIGHT_X + 42, GRID_Y)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.result());
        }

        // The blocks the pattern needs, with counts
        int x = RIGHT_X;
        for (ItemStack part : recipe.parts()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, 47)
                    .setStandardSlotBackground()
                    .addItemStack(part);
            x += 19;
        }
    }

    @Override
    public void draw(RitualDisplay recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        graphics.drawString(font, recipe.ritual().getName(), 2, 2, 0xFF6A3D9A, false);

        // ---- Pattern map: a top-down view, altar in the middle ----
        int r = recipe.radius();
        int cells = r * 2 + 1;
        int cell = Math.min(9, GRID_SIZE / cells);
        int gx = GRID_X + (GRID_SIZE - cells * cell) / 2;
        int gy = GRID_Y + (GRID_SIZE - cells * cell) / 2;

        graphics.fill(gx - 1, gy - 1, gx + cells * cell + 1, gy + cells * cell + 1, 0xFF2A1A3D);
        for (int row = 0; row < cells; row++) {
            for (int col = 0; col < cells; col++) {
                int cx = gx + col * cell;
                int cy = gy + row * cell;
                graphics.fill(cx, cy, cx + cell - 1, cy + cell - 1, 0xFF3B2A52);
            }
        }
        int offset = (cell - 8) / 2;
        drawSmallItem(graphics, new ItemStack(Hollowmancy.RITUAL_ALTAR_ITEM.get()), gx + r * cell + offset, gy + r * cell + offset);
        for (Map.Entry<BlockPos, Item> entry : recipe.grid().entrySet()) {
            int col = entry.getKey().getX() + r;
            int row = entry.getKey().getZ() + r;
            drawSmallItem(graphics, new ItemStack(entry.getValue()), gx + col * cell + offset, gy + row * cell + offset);
        }

        // ---- Right side ----
        if (!recipe.result().isEmpty()) {
            graphics.drawString(font, "\u2192", RIGHT_X + 25, GRID_Y + 5, 0xFF9B5DE5, false);
        }
        graphics.drawString(font, Component.translatable("hollowmancy.ritual_category.pattern"), RIGHT_X, 37, 0xFF777777, false);
        graphics.drawString(font, Component.translatable("jade.hollowmancy.mana", recipe.ritual().getManaCost()), RIGHT_X, 70, 0xFF9B5DE5, false);
        graphics.drawString(font, Component.translatable("hollowmancy.ritual_category.requires", MagicStage.name(recipe.ritual().getRequiredStage())), RIGHT_X, 80, 0xFF555555, false);
        graphics.drawWordWrap(font, FormattedText.of(recipe.effect()), RIGHT_X, 92, WIDTH - RIGHT_X - 2, 0xFF404040);
    }

    /** Draw an item at half size, so a 7x7 pattern fits. */
    private static void drawSmallItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }
}