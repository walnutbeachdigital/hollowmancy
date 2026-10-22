package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.Hollowmancy;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class AscensionRitual extends Ritual {

    public static final int KNOWLEDGE_COST = 20;
    public static final int REQUIRED_MAX_MANA = 200;
    public static final int MAX_MANA_BONUS = 50;

    public AscensionRitual() {
        super("ascension", "Ritual of Ascension",
                "Offer an Echo Shard. Candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, Glimmerstone Ore 3 out on each side, "
                        + "and Crying Obsidian 3 out on each diagonal. Requires " + REQUIRED_MAX_MANA + " max mana. Costs " + KNOWLEDGE_COST + " Knowledge.",
                MagicStage.MASTERY, () -> Items.ECHO_SHARD, 200);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
        requireAll(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get()));
        requireAll(RitualParts.diagonals(3), state -> state.is(Blocks.CRYING_OBSIDIAN));
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        MagicData data = MagicHelper.get(player);
        if (data.stage() >= MagicStage.ASCENSION) {
            player.displayClientMessage(Component.translatable("hollowmancy.ascension_ritual.you_have_already_ascended").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (data.maxMana() < REQUIRED_MAX_MANA) {
            player.displayClientMessage(Component.translatable("hollowmancy.ascension_ritual.your_well_of_mana_is_too", REQUIRED_MAX_MANA)
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (data.knowledge() < KNOWLEDGE_COST) {
            player.displayClientMessage(Component.translatable("hollowmancy.ascension_ritual.you_need_knowledge_to_ascend", KNOWLEDGE_COST)
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data
                .withStage(MagicStage.ASCENSION)
                .withKnowledge(data.knowledge() - KNOWLEDGE_COST)
                .withMaxMana(data.maxMana() + MAX_MANA_BONUS));

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                60, 0.6, 1.0, 0.6, 0.05);
        level.playSound(null, altar, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8f, 1.0f);

        player.sendSystemMessage(Component.translatable("hollowmancy.ascension_ritual.something_vast_turns_to_look_at")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}