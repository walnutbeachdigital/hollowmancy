package com.walnutbeach.hollowmancy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AbyssalFocusItem extends FocusWandItem {

    public AbyssalFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.held_in_main_hand_spells_cost").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.forbidden_spells_leave_half_their_corrup").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.right_click_cast_your_selected_spell").withStyle(ChatFormatting.GRAY));
    }
}