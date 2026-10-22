package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DeepeningRitual extends Ritual {

    public static final int MAX_MANA_CAP = 300;
    public static final int GAIN = 25;

    public DeepeningRitual() {
        super("deepening", "Ritual of Deepening",
                "Offer a Dull Shard. Four lit candles two blocks out on each side, plus four Amethyst Blocks two blocks out on each diagonal.",
                MagicStage.APPRENTICE, Hollowmancy.DULL_SHARD, 60);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        if (MagicHelper.get(player).maxMana() >= MAX_MANA_CAP) {
            player.displayClientMessage(Component.translatable("hollowmancy.deepening_ritual.your_well_of_mana_can_grow")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        MagicData data = MagicHelper.get(player);
        int newMax = Math.min(MAX_MANA_CAP, data.maxMana() + GAIN);
        MagicHelper.set(player, data.withMaxMana(newMax));
        player.sendSystemMessage(Component.translatable("hollowmancy.deepening_ritual.your_well_of_mana_deepens_max", newMax)
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }
}