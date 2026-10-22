package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.client.ClientSpellState;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.network.CastSpellPayload;
import com.walnutbeach.hollowmancy.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class FocusWandItem extends Item {

    public FocusWandItem(Properties properties) {
        super(properties);
    }

    // Right-click casts the selected spell (same as pressing R)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            Spell spell = ClientSpellState.getSelected(player.getData(ModAttachments.MAGIC));
            if (spell != null) {
                int remaining = ClientSpellState.cooldownRemaining(spell.getId(), level.getGameTime());
                if (remaining > 0 && !ClientSpellState.isChanneling()) {
                    player.displayClientMessage(Component.translatable("hollowmancy.focus_wand_item.is_recharging_s", spell.getName(), String.format("%.1f", remaining / 20.0))
                            .withStyle(ChatFormatting.GRAY), true);
                } else {
                    PacketDistributor.sendToServer(new CastSpellPayload(spell.getId()));
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** The wand's name grows with its mastery: "Resonant Focus Wand". */
    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);
        int level = WandMastery.level(stack);
        if (level == 0) return base;
        return Component.translatable("hollowmancy.wand_mastery.title." + level, base)
                .withStyle(level >= WandMastery.MAX_LEVEL ? ChatFormatting.GOLD : ChatFormatting.AQUA);
    }

    /** Counting casts changes the item; don't make the hand dip every time it does. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // enchanted shimmer
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.focus_wand_item.held_in_main_hand_spells_cost").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.right_click_cast_your_selected_spell").withStyle(ChatFormatting.GRAY));
    }
}