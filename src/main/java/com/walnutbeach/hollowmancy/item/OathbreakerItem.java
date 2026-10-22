package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Morvane's blade. Stronger than netherite, and every hit feeds your mana. */
public class OathbreakerItem extends SwordItem {

    public static final int MANA_PER_HIT = 3;

    public OathbreakerItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.4f)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player) {
            MagicData data = MagicHelper.get(player);
            if (data.mana() < data.maxMana()) MagicHelper.set(player, data.withMana(data.mana() + MANA_PER_HIT));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.oathbreaker_item.every_hit_restores_mana", MANA_PER_HIT).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.oathbreaker_item.he_broke_every_oath_but_one").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}
