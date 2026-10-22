package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.spell.CraftedSpell;
import com.walnutbeach.hollowmancy.spell.CraftedSpell.Element;
import com.walnutbeach.hollowmancy.spell.CraftedSpell.Modifier;
import com.walnutbeach.hollowmancy.spell.CraftedSpell.Shape;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** A Blank Scroll + one shape rune + one element rune + (optionally) one modifier rune = an Inscribed Scroll. */
public class SpellScrollRecipe extends CustomRecipe {

    public SpellScrollRecipe(CraftingBookCategory category) {
        super(category);
    }

    private record Parts(Shape shape, Element element, Modifier modifier) {}

    private static Parts read(CraftingInput input) {
        int scrolls = 0;
        Shape shape = null;
        Element element = null;
        Modifier modifier = Modifier.NONE;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(SpellCraftingItems.BLANK_SCROLL.get())) {
                scrolls++;
                continue;
            }
            Shape s = null; Element e = null; Modifier m = null;
            for (Shape x : Shape.values()) if (stack.is(x.rune.get())) s = x;
            for (Element x : Element.values()) if (stack.is(x.rune.get())) e = x;
            for (Modifier x : Modifier.values()) if (x.rune != null && stack.is(x.rune.get())) m = x;
            if (s != null) { if (shape != null) return null; shape = s; }
            else if (e != null) { if (element != null) return null; element = e; }
            else if (m != null) { if (modifier != Modifier.NONE) return null; modifier = m; }
            else return null;   // something that doesn't belong
        }
        return scrolls == 1 && shape != null && element != null ? new Parts(shape, element, modifier) : null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return read(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Parts parts = read(input);
        if (parts == null) return ItemStack.EMPTY;
        return InscribedScrollItem.of(CraftedSpell.id(parts.shape(), parts.element(), parts.modifier()));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpellCraftingItems.SCROLL_RECIPE;
    }
}
