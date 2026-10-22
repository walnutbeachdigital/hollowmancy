package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class VerdantGrowthSpell extends Spell {

    public VerdantGrowthSpell() {
        super("verdant_growth", "Verdant Growth", "Crops, saplings and flowers within 4 blocks surge with life.",
                MagicSchool.NATURE, MagicStage.ARCANE_ARTS, 4, 25, 100);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Reaches 5 blocks, and each plant surges twice."
                : "Reaches 6 blocks, and each plant surges three times.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        int radius = tier == 1 ? 4 : tier == 2 ? 5 : 6;
        int surges = tier;

        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        int grown = 0;

        for (BlockPos mutable : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
            BlockPos pos = mutable.immutable();
            BlockState state = level.getBlockState(pos);
            if (isSpreadingGround(state)) continue; // don't carpet the area in flowers
            if (!(state.getBlock() instanceof BonemealableBlock)) continue;

            boolean grewHere = false;
            for (int i = 0; i < surges; i++) {
                BlockState current = level.getBlockState(pos);
                if (!(current.getBlock() instanceof BonemealableBlock plant)) break;
                if (!plant.isValidBonemealTarget(level, pos, current)) break;
                plant.performBonemeal(level, level.random, pos, current);
                grewHere = true;
            }
            if (grewHere) {
                grown++;
                level.sendParticles(HollowmancyParticles.LEAF.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        6, 0.3, 0.3, 0.3, 0.0);
            }
        }

        if (grown == 0) {
            player.displayClientMessage(Component.translatable("hollowmancy.verdant_growth_spell.nothing_here_is_ready_to_grow")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        level.sendParticles(HollowmancyParticles.LEAF.get(), player.getX(), player.getY() + 0.5, player.getZ(),
                30, radius / 2.0, 0.3, radius / 2.0, 0.0);
        SpellFx.flourish(level, getSchool(), player.position(), radius);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    private static boolean isSpreadingGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM);
    }
}