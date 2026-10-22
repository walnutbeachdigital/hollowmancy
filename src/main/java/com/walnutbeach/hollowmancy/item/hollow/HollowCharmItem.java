package com.walnutbeach.hollowmancy.item.hollow;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A charm made from a Hollow creature. It works while it's anywhere in your inventory. What it does is in HollowCharms. */
public class HollowCharmItem extends Item {
    public enum Kind { WISPBOUND, MOTHWING, HUNTSMAN, SHADEWEAVE, TIDEBOUND }

    private final Kind kind;

    public HollowCharmItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public static boolean has(Player player, Kind kind) {
        return player.getInventory().hasAnyMatching(stack -> stack.getItem() instanceof HollowCharmItem charm && charm.kind == kind);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String key = "item.hollowmancy." + kind.name().toLowerCase(java.util.Locale.ROOT) + "_charm.effect";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.hollowmancy.hollow_charm.anywhere").withStyle(ChatFormatting.DARK_GRAY));
    }
}
