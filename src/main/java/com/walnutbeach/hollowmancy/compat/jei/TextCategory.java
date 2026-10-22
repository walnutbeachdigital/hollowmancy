package com.walnutbeach.hollowmancy.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/** Shared layout for Hollowmancy's simple pages: a title line, slots up top, wrapped text below. */
public abstract class TextCategory<T> implements IRecipeCategory<T> {

    protected static final int WIDTH = 176;
    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable icon;
    private final int height;

    protected TextCategory(RecipeType<T> type, String title, IDrawable icon, int height) {
        this.type = type;
        this.title = Component.literal(title);
        this.icon = icon;
        this.height = height;
    }

    @Override public RecipeType<T> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return height; }

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    /** Wrapped text; returns the y below it. */
    protected static int text(GuiGraphics graphics, String text, int x, int y, int color) {
        Font font = font();
        graphics.drawWordWrap(font, FormattedText.of(text), x, y, WIDTH - x - 2, color);
        return y + font.wordWrapHeight(FormattedText.of(text), WIDTH - x - 2) + 2;
    }
}
