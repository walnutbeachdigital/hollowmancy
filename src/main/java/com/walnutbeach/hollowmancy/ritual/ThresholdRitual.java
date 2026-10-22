package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ThresholdRitual extends Ritual {

    public static final int KNOWLEDGE_COST = 10;

    public ThresholdRitual() {
        super("threshold", "Ritual of the Threshold",
                "Offer an Amethyst Shard. Four lit candles 2 blocks out on each side, four Amethyst Blocks 2 out on each diagonal, "
                        + "and four Lapis Blocks 3 out on each side. Costs " + KNOWLEDGE_COST + " Knowledge.",
                MagicStage.APPRENTICE, () -> Items.AMETHYST_SHARD, 100);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
        requireAll(RitualParts.cardinals(3), state -> state.is(Blocks.LAPIS_BLOCK));
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        MagicData data = MagicHelper.get(player);
        if (data.stage() >= MagicStage.ARCANE_ARTS) {
            player.displayClientMessage(Component.translatable("hollowmancy.threshold_ritual.you_have_already_crossed_the_threshold")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (data.knowledge() < KNOWLEDGE_COST) {
            player.displayClientMessage(Component.translatable("hollowmancy.threshold_ritual.you_need_knowledge_to_cross_the", KNOWLEDGE_COST)
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data
                .withStage(MagicStage.ARCANE_ARTS)
                .withKnowledge(data.knowledge() - KNOWLEDGE_COST));

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(),
                80, 0.5, 1.0, 0.5, 0.4);
        level.playSound(null, altar, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(Component.translatable("hollowmancy.threshold_ritual.the_world_opens_like_a_book")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }
}