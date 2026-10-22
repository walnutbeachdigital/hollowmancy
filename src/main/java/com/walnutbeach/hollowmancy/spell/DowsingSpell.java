package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class DowsingSpell extends Spell {

    private static final String[] COMPASS = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};

    public DowsingSpell() {
        super("dowsing", "Dowsing", "Sense the nearest ore within 12 blocks, and which way it lies.",
                MagicSchool.DIVINATION, MagicStage.ARCANE_ARTS, 5, 25, 100);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Senses ore within 16 blocks." : "Senses ore within 24 blocks.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        int radius = tier == 1 ? 12 : tier == 2 ? 16 : 24;

        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();

        BlockPos best = null;
        BlockState bestState = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Tags.Blocks.ORES) || state.is(Hollowmancy.GLIMMERSTONE_ORE.get()) || state.is(Hollowmancy.VOIDSTONE_ORE.get())) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                    bestState = state;
                }
            }
        }

        if (best == null) {
            player.displayClientMessage(Component.translatable("hollowmancy.dowsing_spell.you_sense_no_ore_nearby").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        int dx = best.getX() - origin.getX();
        int dy = best.getY() - origin.getY();
        int dz = best.getZ() - origin.getZ();
        int distance = (int) Math.round(Math.sqrt(bestDist));

        String vertical = dy < -1 ? (-dy) + " below, " : dy > 1 ? dy + " above, " : "";
        String horizontal;
        if (dx * dx + dz * dz < 2) {
            horizontal = "right here";
        } else {
            double bearing = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0) % 360.0;
            horizontal = COMPASS[(int) Math.round(bearing / 45.0) % 8];
        }

        player.displayClientMessage(Component.empty()
                .append(bestState.getBlock().getName())
                .append(" - " + distance + " blocks (" + vertical + horizontal + ")")
                .withStyle(ChatFormatting.GOLD), true);

        Vec3 eye = player.getEyePosition();
        Vec3 toward = Vec3.atCenterOf(best).subtract(eye).normalize();
        for (double d = 1.0; d <= Math.min(5.0, distance); d += 0.4) {
            Vec3 point = eye.add(toward.scale(d));
            level.sendParticles(HollowmancyParticles.DIVINE_MOTE.get(), point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 2.0f);
        return true;
    }
}