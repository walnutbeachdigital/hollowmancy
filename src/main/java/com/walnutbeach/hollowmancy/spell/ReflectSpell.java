package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

/** Warding. Learned only from the Tome of Reflection, found in the Mirror Sea. */
public class ReflectSpell extends Spell {

    public ReflectSpell() {
        super("reflect", "Reflect",
            "For 6 seconds, arrows, fireballs and tridents bounce back at whoever threw them, and half of any spell cast at you "
                + "is turned back on its caster. Learned only from the Tome of Reflection.",
            MagicSchool.WARDING, MagicStage.MASTERY, 1, 30, 400);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Lasts 9 seconds." : "Lasts 12 seconds, and turns back all of a spell, not half.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        Reflection.start(player, tier == 1 ? 120 : tier == 2 ? 180 : 240, tier);
        ServerLevel level = player.serverLevel();
        level.sendParticles(HollowmancyParticles.WARD_GLINT.get(), player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.7, 1.0, 0.7, 0.03);
        SpellFx.flourish(level, getSchool(), player.position(), 1.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 1.8f);
        return true;
    }
}
