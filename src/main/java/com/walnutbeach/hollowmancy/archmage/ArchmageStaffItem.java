package com.walnutbeach.hollowmancy.archmage;

import com.walnutbeach.hollowmancy.item.FocusWandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The Archmage's Staff: the best focus there is. Spells through it cost 40% less mana, gather half the
 * corruption and recharge 10% faster. It is a wand, so it grows with use like any other.
 */
public class ArchmageStaffItem extends FocusWandItem {
    public static final float DISCOUNT = 0.40F;
    public static final float COOLDOWN = 0.90F;

    public ArchmageStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.archmage_staff.tooltip.cost").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.archmage_staff.tooltip.corruption").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.archmage_staff.tooltip.cooldown").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.right_click_cast_your_selected_spell").withStyle(ChatFormatting.GRAY));
    }
}
