package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class RevealSpell extends Spell {

    public RevealSpell() {
        super("reveal", "Reveal", "Every creature within 24 blocks glows through walls for 10 seconds.",
                MagicSchool.DIVINATION, MagicStage.ARCANE_ARTS, 4, 30, 200);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Sees 36 blocks, for 15 seconds." : "Sees 48 blocks, for 20 seconds.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 24.0 : tier == 2 ? 36.0 : 48.0;
        int duration = tier == 1 ? 200 : tier == 2 ? 300 : 400;

        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false));
        }

        player.displayClientMessage(Component.translatable("hollowmancy.reveal_spell.revealed_creatures", targets.size())
                .withStyle(ChatFormatting.GOLD), true);
        level.sendParticles(HollowmancyParticles.DIVINE_MOTE.get(), player.getX(), player.getY() + 1.5, player.getZ(),
                40, 1.0, 1.0, 1.0, 0.03);
        SpellFx.flourish(level, getSchool(), player.position(), 4.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.8f, 1.2f);
        return true;
    }
}