package com.walnutbeach.hollowmancy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SovereignCrownItem extends Item {

    public SovereignCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.sovereign_crown_item.kept_in_your_inventory").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hollowmancy.sovereign_crown_item.corruption_can_no_longer_harm_you").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("hollowmancy.sovereign_crown_item.mana_returns_twice_as_fast").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.sovereign_crown_item.no_one_should_hold_it_alone").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}