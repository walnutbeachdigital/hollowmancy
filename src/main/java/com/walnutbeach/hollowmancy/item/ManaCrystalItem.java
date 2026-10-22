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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ManaCrystalItem extends Item {

    public static final int CAPACITY = 200;
    public static final int TRANSFER = 25;

    public ManaCrystalItem(Properties properties) {
        super(properties);
    }

    public static int getStored(ItemStack stack) {
        return stack.getOrDefault(ModComponents.STORED_MANA.get(), 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        MagicData data = MagicHelper.get(serverPlayer);
        int stored = getStored(stack);

        if (player.isShiftKeyDown()) {
            // Deposit: player -> crystal
            int amount = Math.min(TRANSFER, Math.min(data.mana(), CAPACITY - stored));
            if (amount <= 0) {
                player.displayClientMessage((stored >= CAPACITY ? Component.translatable("hollowmancy.mana_crystal_item.the_crystal_is_full") : Component.translatable("hollowmancy.mana_crystal_item.you_have_no_mana_to_give"))
                        .withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            stack.set(ModComponents.STORED_MANA.get(), stored + amount);
            MagicHelper.set(serverPlayer, data.withMana(data.mana() - amount));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.6f);
        } else {
            // Withdraw: crystal -> player
            int amount = Math.min(TRANSFER, Math.min(stored, data.maxMana() - data.mana()));
            if (amount <= 0) {
                player.displayClientMessage((stored <= 0 ? Component.translatable("hollowmancy.mana_crystal_item.the_crystal_is_empty") : Component.translatable("hollowmancy.mana_crystal_item.your_mana_is_already_full"))
                        .withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            stack.set(ModComponents.STORED_MANA.get(), stored - amount);
            MagicHelper.set(serverPlayer, data.withMana(data.mana() + amount));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.4f);
        }
        return InteractionResultHolder.consume(stack);
    }

    // The fill bar under the item (like tool durability)
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getStored(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x9B5DE5;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.mana_crystal_item.stored_mana", getStored(stack), CAPACITY).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.mana_crystal_item.right_click_draw_mana", TRANSFER).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hollowmancy.mana_crystal_item.sneak_right_click_store_mana", TRANSFER).withStyle(ChatFormatting.GRAY));
    }
}