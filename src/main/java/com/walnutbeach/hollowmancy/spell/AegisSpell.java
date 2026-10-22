package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class AegisSpell extends Spell {

    public AegisSpell() {
        super("aegis", "Aegis", "Wrap yourself in a ward that turns aside harm for 15 seconds.",
                MagicSchool.WARDING, MagicStage.ARCANE_ARTS, 4, 35, 400);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Lasts 20 seconds." : "Lasts 25 seconds, with a stronger shield.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        int duration = tier == 1 ? 300 : tier == 2 ? 400 : 500;
        int absorption = tier == 3 ? 2 : 1;

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, absorption));

        ServerLevel level = player.serverLevel();
        level.sendParticles(HollowmancyParticles.WARD_GLINT.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                30, 0.6, 0.8, 0.6, 0.02);
        SpellFx.flourish(level, getSchool(), player.position(), 2.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7f, 1.6f);
        return true;
    }
}