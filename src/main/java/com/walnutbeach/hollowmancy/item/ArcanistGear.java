package com.walnutbeach.hollowmancy.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Every Arcanist's Garb bonus in one place. */
public class ArcanistGear {

    public static final float HOOD_MANA_MULTIPLIER = 0.90f;      // spells cost 10% less
    public static final float ROBE_CORRUPTION_MULTIPLIER = 0.75f; // 25% less corruption
    public static final float LEGGINGS_REGEN_MULTIPLIER = 1.50f;  // 50% faster mana regen
    public static final float BOOTS_CHARGE_MULTIPLIER = 0.75f;    // channels 25% faster
    public static final float SET_COOLDOWN_MULTIPLIER = 0.85f;    // full set: cooldowns 15% shorter
    public static final int SET_DECAY_DIVISOR = 3;                // full set: corruption fades 3x faster

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static boolean isArcanist(ItemStack stack) {
        return stack.getItem() instanceof ArcanistArmorItem;
    }

    public static boolean wearing(Player player, EquipmentSlot slot) {
        return isArcanist(player.getItemBySlot(slot));
    }

    public static int pieces(Player player) {
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (wearing(player, slot)) count++;
        }
        return count;
    }

    public static boolean fullSet(Player player) {
        return pieces(player) == ARMOR_SLOTS.length;
    }

    public static int manaCost(Player player, int cost) {
        return wearing(player, EquipmentSlot.HEAD) ? Math.max(1, Math.round(cost * HOOD_MANA_MULTIPLIER)) : cost;
    }

    public static int corruption(Player player, int corruption) {
        if (corruption <= 0 || !wearing(player, EquipmentSlot.CHEST)) return corruption;
        return Math.max(1, Math.round(corruption * ROBE_CORRUPTION_MULTIPLIER));
    }

    public static int regen(Player player, int regen) {
        return com.walnutbeach.hollowmancy.item.hollow.HollowCharms.regen(player, wearing(player, EquipmentSlot.LEGS) ? Math.round(regen * LEGGINGS_REGEN_MULTIPLIER) : regen);
    }

    public static int charge(Player player, int ticks) {
        if (ticks <= 0 || !wearing(player, EquipmentSlot.FEET)) return ticks;
        return Math.max(1, Math.round(ticks * BOOTS_CHARGE_MULTIPLIER));
    }

    public static int cooldown(Player player, int ticks) {
        return fullSet(player) ? Math.max(1, Math.round(ticks * SET_COOLDOWN_MULTIPLIER)) : ticks;
    }

    /** How many ticks between corruption losing a point. */
    public static int decayInterval(Player player, int interval) {
        return fullSet(player) ? Math.max(1, interval / SET_DECAY_DIVISOR) : interval;
    }
}