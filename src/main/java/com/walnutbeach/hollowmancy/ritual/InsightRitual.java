package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class InsightRitual extends Ritual {

    public InsightRitual() {
        super("insight", "Ritual of Insight",
                "Offer a Book. Four lit candles, two blocks from the altar on each side (north, south, east, west).",
                MagicStage.APPRENTICE, () -> Items.BOOK, 40);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data.withKnowledge(data.knowledge() + 3));
        player.sendSystemMessage(Component.translatable("hollowmancy.insight_ritual.the_pages_blaze_and_are_gone")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }
}