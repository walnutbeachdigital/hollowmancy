package com.walnutbeach.hollowmancy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WardingAmuletItem extends Item {

    public WardingAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.warding_amulet_item.keep_in_your_inventory_absorbs_30").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("hollowmancy.warding_amulet_item.each_point_absorbed_costs_5_mana").withStyle(ChatFormatting.GRAY));
    }
}