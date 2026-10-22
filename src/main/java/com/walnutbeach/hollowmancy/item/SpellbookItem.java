package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.client.SpellbookClient;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpellbookItem extends Item {

    public SpellbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (MagicHelper.hasStage(player, MagicStage.AWAKENING)) {
                SpellbookClient.open();
            } else {
                player.displayClientMessage(Component.translatable("hollowmancy.spell_tome_item.the_pages_are_blank_to_you")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.spellbook_item.right_click_to_study_and_prepare").withStyle(ChatFormatting.GRAY));
    }
}