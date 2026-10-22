package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class MendSpell extends Spell {

    public MendSpell() {
        super("mend", "Mend", "Knit your wounds closed. Restores 3 hearts.",
                MagicStage.APPRENTICE, 2, 30, 100);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Restores 4 hearts."
                : "Restores 5 hearts and purges poison and withering.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        float heal = tier == 1 ? 6.0f : tier == 2 ? 8.0f : 10.0f;
        boolean canPurge = tier >= 3 && (player.hasEffect(MobEffects.POISON) || player.hasEffect(MobEffects.WITHER));

        if (player.getHealth() >= player.getMaxHealth() && !canPurge) {
            player.displayClientMessage(Component.translatable("hollowmancy.mend_spell.you_are_already_whole")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        player.heal(heal);
        if (tier >= 3) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
        }

        ServerLevel level = player.serverLevel();
        level.sendParticles(HollowmancyParticles.HEAL_MOTE.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.4, 0.5, 0.4, 0.0);
        level.sendParticles(HollowmancyParticles.RUNE_ARCANE.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                6, 0.5, 0.6, 0.5, 0.01);
        SpellFx.ring(level, HollowmancyParticles.RING_NATURE.get(), player.position().add(0, 0.08, 0), 1.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.7f);
        return true;
    }
}