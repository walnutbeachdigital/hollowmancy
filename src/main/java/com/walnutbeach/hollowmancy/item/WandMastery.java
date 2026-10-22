package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.HollowmancyAdvancements;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Wands grow with use. Every spell cast while holding a wand counts; at each milestone it gains a level,
 * a new name and a little more power: spells cast through it cost less mana and recharge faster.
 */
public final class WandMastery {
    /** Casts needed for each level (0 = a fresh wand). */
    public static final int[] CASTS = {0, 40, 120, 300, 600, 1000};
    public static final int MAX_LEVEL = CASTS.length - 1;
    /** Per level: 2% cheaper and 3% faster to recharge (10% and 15% at the top). */
    public static final float COST_PER_LEVEL = 0.02F;
    public static final float COOLDOWN_PER_LEVEL = 0.03F;
    private static final String KEY = "hollowmancy_casts";

    public static boolean isWand(ItemStack stack) {
        return stack.getItem() instanceof FocusWandItem;
    }

    public static int casts(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY);
    }

    /** Casts counted toward growth: the real count times the "wandMasterySpeed" setting. */
    private static double effective(ItemStack stack) {
        return casts(stack) * HollowmancyExtraConfig.get(HollowmancyExtraConfig.WAND_MASTERY_SPEED);
    }

    public static int level(ItemStack stack) {
        if (!isWand(stack)) return 0;
        double casts = effective(stack);
        int level = 0;
        for (int i = 1; i < CASTS.length; i++) if (casts >= CASTS[i]) level = i;
        return level;
    }

    /** Casts still needed for the next level, or 0 at the top. */
    public static int toNext(ItemStack stack) {
        int level = level(stack);
        if (level >= MAX_LEVEL) return 0;
        double speed = HollowmancyExtraConfig.get(HollowmancyExtraConfig.WAND_MASTERY_SPEED);
        return (int) Math.ceil((CASTS[level + 1] - effective(stack)) / speed);
    }

    public static int cost(Player player, int cost) {
        int level = level(player.getMainHandItem());
        return level == 0 ? cost : Math.max(1, Math.round(cost * (1.0F - level * COST_PER_LEVEL)));
    }

    public static int cooldown(Player player, int ticks) {
        int level = level(player.getMainHandItem());
        return level == 0 ? ticks : Math.max(1, Math.round(ticks * (1.0F - level * COOLDOWN_PER_LEVEL)));
    }

    /** Called by Spells.release after a spell is cast. */
    public static void onCast(ServerPlayer player) {
        ItemStack wand = player.getMainHandItem();
        if (!isWand(wand)) return;
        int before = level(wand);
        CustomData.update(DataComponents.CUSTOM_DATA, wand, tag -> tag.putInt(KEY, tag.getInt(KEY) + 1));
        int after = level(wand);
        if (after <= before) return;
        HollowmancyAdvancements.award(player, "wand_grows", "grown");
        if (after >= MAX_LEVEL) HollowmancyAdvancements.award(player, "wand_master", "mastered");

        player.sendSystemMessage(Component.translatable("hollowmancy.wand_mastery.level_up", wand.getHoverName(),
                Component.translatable("hollowmancy.wand_mastery.level." + after)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        SpellFx.flourish(player.serverLevel(), MagicSchool.GENERAL, player.position(), 2.0F + after * 0.4F);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.2F + after * 0.1F);
    }

    private WandMastery() {}
}
