package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.world.item.ItemStack;

/** Items that store mana of their own (Mana Crystals, Wellspring Wands). */
public class ManaVessels {

    public static int capacity(ItemStack stack) {
        if (stack.is(Hollowmancy.MANA_CRYSTAL.get())) return ManaCrystalItem.CAPACITY;
        if (stack.is(Hollowmancy.WELLSPRING_WAND.get())) return WellspringWandItem.CAPACITY;
        return 0;
    }

    public static boolean isVessel(ItemStack stack) {
        return capacity(stack) > 0;
    }

    public static int stored(ItemStack stack) {
        return stack.getOrDefault(ModComponents.STORED_MANA.get(), 0);
    }

    public static void setStored(ItemStack stack, int amount) {
        stack.set(ModComponents.STORED_MANA.get(), amount);
    }
}