package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.LeyLines;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class LeySightSpell extends Spell {

    private static final String[] COMPASS = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};

    public LeySightSpell() {
        super("ley_sight", "Ley Sight",
            "See the ley lines around you for 15 seconds, and sense where the strongest current runs within 48 blocks.",
            MagicSchool.DIVINATION, MagicStage.ARCANE_ARTS, 4, 20, 300);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Senses 72 blocks; the sight lasts 22 seconds." : "Senses 96 blocks; the sight lasts 30 seconds.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.translatable("hollowmancy.ley_sight_spell.no_ley_lines_run_through_this")
                .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        int tier = tier(player);
        int radius = tier == 1 ? 48 : tier == 2 ? 72 : 96;
        int duration = tier == 1 ? 300 : tier == 2 ? 450 : 600;
        LeyLines.startSight(player, duration);

        BlockPos here = player.blockPosition();
        float strength = LeyLines.strength(level, here);
        String hereText = strength > 0.05f ? "Ley current here: " + LeyLines.percent(strength) + "%" : "No current beneath you";

        BlockPos best = LeyLines.findStrongest(level, here, radius);
        String bestText;
        if (best == null) {
            bestText = ". Nothing strong within " + radius + " blocks.";
        } else {
            int dx = best.getX() - here.getX(), dz = best.getZ() - here.getZ();
            int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            if (distance < 4) {
                bestText = ". You stand at its heart.";
            } else {
                double bearing = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0) % 360.0;
                bestText = ". Strongest " + LeyLines.percent(LeyLines.strength(level, best)) + "% at " + distance
                    + " blocks " + COMPASS[(int) Math.round(bearing / 45.0) % 8] + ".";
                Vec3 eye = player.getEyePosition();
                Vec3 toward = new Vec3(dx, 0, dz).normalize();
                for (double d = 1.0; d <= 5.0; d += 0.4) {
                    Vec3 point = eye.add(toward.scale(d));
                    level.sendParticles(player, HollowmancyParticles.DIVINE_MOTE.get(), true, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                }
            }
        }

        player.displayClientMessage(Component.literal(hereText + bestText).withStyle(ChatFormatting.AQUA), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.6f);
        return true;
    }
}
