package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

public class CleansingRitual extends Ritual {

    public static final int CLEANSE_AMOUNT = 50;

    public CleansingRitual() {
        super("cleansing", "Ritual of Cleansing",
                "Offer a Golden Apple. Four lit candles 2 out on each side, four Amethyst Blocks 2 out on each diagonal. Removes "
                        + CLEANSE_AMOUNT + " corruption.",
                MagicStage.ASCENSION, () -> Items.GOLDEN_APPLE, 60);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        if (MagicHelper.get(player).corruption() <= 0) {
            player.displayClientMessage(Component.translatable("hollowmancy.cleansing_ritual.there_is_no_corruption_in_you")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        MagicData data = MagicHelper.get(player);
        int newCorruption = Math.max(0, data.corruption() - CLEANSE_AMOUNT);
        MagicHelper.set(player, data.withCorruption(newCorruption));

        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.HUNGER);

        player.sendSystemMessage(Component.translatable("hollowmancy.cleansing_ritual.the_whispers_fall_quiet_corruption", newCorruption)
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }
}