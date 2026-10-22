package com.walnutbeach.hollowmancy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TreasuryKeyItem extends Item {

    public TreasuryKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.treasury_key_item.opens_the_vault_behind_morvane_s").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("hollowmancy.treasury_key_item.right_click_the_vault_door_with").withStyle(ChatFormatting.GRAY));
    }
}
