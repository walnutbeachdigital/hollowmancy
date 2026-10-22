package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class RecallSpell extends Spell {

    public RecallSpell() {
        super("recall", "Recall", "Return to your bed or spawn point from anywhere, even another dimension.",
                MagicSchool.DIVINATION, MagicStage.MASTERY, 7, 80, 1200);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Arrive shielded: Resistance for 5 seconds."
                : "Arrive shielded and mending: Resistance and Regeneration for 10 seconds.";
    }

    @Override
    public int getChargeTicks() {
        return 60; // 3 seconds
    }

    @Override
    public boolean isInterruptedByDamage() {
        return true;
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        BlockPos respawn = player.getRespawnPosition();
        ServerLevel targetLevel = server.getLevel(player.getRespawnDimension());
        BlockPos target;
        if (respawn != null && targetLevel != null) {
            target = respawn.above();
        } else {
            targetLevel = server.overworld();
            BlockPos spawn = targetLevel.getSharedSpawnPos();
            int y = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
            target = new BlockPos(spawn.getX(), y, spawn.getZ());
        }

        BlockPos safe = findStandingSpot(targetLevel, player, target);
        if (safe == null) {
            player.displayClientMessage(Component.translatable("hollowmancy.recall_spell.something_blocks_your_way_home")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        ServerLevel from = player.serverLevel();
        from.sendParticles(HollowmancyParticles.DIVINE_MOTE.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                50, 0.5, 1.0, 0.5, 0.03);
        SpellFx.flourish(from, getSchool(), player.position(), 2.0F);
        from.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.teleportTo(targetLevel, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.resetFallDistance();

        if (tier == 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        } else if (tier == 3) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        }

        targetLevel.sendParticles(HollowmancyParticles.DIVINE_MOTE.get(), safe.getX() + 0.5, safe.getY() + 1.0, safe.getZ() + 0.5,
                50, 0.5, 1.0, 0.5, 0.03);
        SpellFx.flourish(targetLevel, getSchool(), Vec3.atBottomCenterOf(safe), 2.0F);
        targetLevel.playSound(null, safe, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.2f);
        return true;
    }

    private static BlockPos findStandingSpot(ServerLevel level, ServerPlayer player, BlockPos target) {
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos candidate = target.offset(dx, dy, dz);
                    AABB box = player.getDimensions(player.getPose()).makeBoundingBox(Vec3.atBottomCenterOf(candidate));
                    if (level.noCollision(box) && !level.getBlockState(candidate.below()).isAir()) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}