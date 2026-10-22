package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class WellspringWandItem extends FocusWandItem {

    public static final int CAPACITY = 300;
    public static final int TRANSFER = 25;

    public WellspringWandItem(Properties properties) {
        super(properties);
    }

    // Sneak + right-click: pour your mana in. Right-click: cast, as with any Focus Wand.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MagicData data = MagicHelper.get(serverPlayer);
            int stored = ManaVessels.stored(stack);
            int amount = Math.min(TRANSFER, Math.min(data.mana(), CAPACITY - stored));
            if (amount > 0) {
                ManaVessels.setStored(stack, stored + amount);
                MagicHelper.set(serverPlayer, data.withMana(data.mana() - amount));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            serverPlayer.displayClientMessage(Component.translatable("hollowmancy.wellspring_wand_item.wellspring", ManaVessels.stored(stack), CAPACITY)
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * ManaVessels.stored(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x7FE0E8;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.mana_crystal_item.stored_mana", ManaVessels.stored(stack), CAPACITY).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("hollowmancy.wellspring_wand_item.held_in_main_hand_spells_cost").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.abyssal_focus_item.right_click_cast_your_selected_spell").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hollowmancy.wellspring_wand_item.sneak_right_click_store_of_your", TRANSFER).withStyle(ChatFormatting.GRAY));
    }
}