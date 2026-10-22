package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.Listener;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import com.walnutbeach.hollowmancy.item.EndgameItems;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.VeilStepSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

/** The last ritual: call the source of the Hollow by name. */
public class UnmakingRitual extends Ritual {

    public UnmakingRitual() {
        super("unmaking", "Ritual of Unmaking",
            "Only in the Hollow. Offer the Seal of Unmaking. Candles 2 out on each side, Crying Obsidian 2 out on each diagonal, "
                + "Amethyst Blocks 3 out on each side, Voidstone Ore 3 out on each diagonal, Gold Blocks 4 out on each side.",
            MagicStage.ASCENSION, () -> EndgameItems.SEAL_OF_UNMAKING.get(), 300);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), state -> state.is(Blocks.CRYING_OBSIDIAN));
        requireAll(RitualParts.cardinals(3), RitualParts.AMETHYST_BLOCK);
        requireAll(RitualParts.diagonals(3), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get()));
        requireAll(RitualParts.cardinals(4), state -> state.is(Blocks.GOLD_BLOCK));
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        if (!player.level().dimension().equals(VeilStepSpell.HOLLOW)) {
            player.displayClientMessage(Component.translatable("hollowmancy.unmaking_ritual.it_cannot_hear_you_here_only")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        if (!player.serverLevel().getEntitiesOfClass(Listener.class, player.getBoundingBox().inflate(160)).isEmpty()) {
            player.displayClientMessage(Component.translatable("hollowmancy.unmaking_ritual.it_is_already_listening").withStyle(ChatFormatting.DARK_PURPLE), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        Listener listener = ModEntities.LISTENER.get().create(level);
        if (listener == null) return;
        listener.moveTo(altar.getX() + 0.5, altar.getY() + 6, altar.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        level.addFreshEntity(listener);
        for (ServerPlayer near : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(64))) {
            near.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
            near.sendSystemMessage(Component.translatable("hollowmancy.unmaking_ritual.you_have_called_it_by_name")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, altar.getX() + 0.5, altar.getY() + 6, altar.getZ() + 0.5, 200, 2, 2, 2, 0.5);
        level.playSound(null, altar, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0f, 0.4f);
    }
}
