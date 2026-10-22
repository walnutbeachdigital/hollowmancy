package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.AmethystColossus;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ResonanceRitual extends Ritual {

    public ResonanceRitual() {
        super("resonance", "Ritual of Resonance",
                "Only in the Crystal Barrens. Offer a Resonant Core. Candles 2 out on each side; Amethyst Blocks 2 out on each "
                        + "diagonal, and 3 out on each side and each diagonal.",
                MagicStage.ASCENSION, Hollowmancy.RESONANT_CORE, 200);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
        requireAll(RitualParts.cardinals(3), RitualParts.AMETHYST_BLOCK);
        requireAll(RitualParts.diagonals(3), RitualParts.AMETHYST_BLOCK);
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        if (!player.level().getBiome(player.blockPosition()).is(AmethystColossus.CRYSTAL_BARRENS)) {
            player.displayClientMessage(Component.translatable("hollowmancy.resonance_ritual.the_crystal_here_is_silent_only")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        if (!player.serverLevel().getEntitiesOfClass(AmethystColossus.class, player.getBoundingBox().inflate(128)).isEmpty()) {
            player.displayClientMessage(Component.translatable("hollowmancy.resonance_ritual.it_is_already_singing").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        AmethystColossus colossus = ModEntities.AMETHYST_COLOSSUS.get().create(level);
        if (colossus == null) return;
        colossus.moveTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        colossus.setTarget(player);
        level.addFreshEntity(colossus);

        level.sendParticles(ParticleTypes.END_ROD, altar.getX() + 0.5, altar.getY() + 2.0, altar.getZ() + 0.5,
                80, 1.0, 2.0, 1.0, 0.1);
        level.playSound(null, altar, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0f, 0.4f);
        player.sendSystemMessage(Component.translatable("hollowmancy.resonance_ritual.the_crystals_begin_to_sing")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }
}