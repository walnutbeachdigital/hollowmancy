package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.HollowSovereign;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.VeilStepSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Blocks;

public class WakingRitual extends Ritual {

    public WakingRitual() {
        super("waking", "Ritual of Waking",
                "Only in the Hollow. Offer a Void Heart. Candles 2 out on each side, Voidstone Ore 2 out on each diagonal "
                        + "and 3 out on each side, Crying Obsidian 3 out on each diagonal.",
                MagicStage.ASCENSION, Hollowmancy.VOID_HEART, 200);
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get()));
        requireAll(RitualParts.cardinals(3), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get()));
        requireAll(RitualParts.diagonals(3), state -> state.is(Blocks.CRYING_OBSIDIAN));
    }

    @Override
    public boolean canPerform(ServerPlayer player) {
        if (!player.level().dimension().equals(VeilStepSpell.HOLLOW)) {
            player.displayClientMessage(Component.translatable("hollowmancy.waking_ritual.he_cannot_hear_you_from_here")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        if (!player.serverLevel().getEntitiesOfClass(HollowSovereign.class, player.getBoundingBox().inflate(128)).isEmpty()) {
            player.displayClientMessage(Component.translatable("hollowmancy.waking_ritual.he_is_already_awake")
                    .withStyle(ChatFormatting.DARK_RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        HollowSovereign boss = ModEntities.HOLLOW_SOVEREIGN.get().create(level);
        if (boss == null) return;
        boss.moveTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        boss.setTarget(player);
        level.addFreshEntity(boss);

        level.playSound(null, altar, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.6f);
        player.sendSystemMessage(Component.translatable("hollowmancy.waking_ritual.something_vast_opens_its_eyes")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}