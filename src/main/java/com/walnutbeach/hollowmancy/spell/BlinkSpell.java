package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class BlinkSpell extends Spell {

    public BlinkSpell() {
        super("blink", "Blink", "Step through space to where you are looking, up to 8 blocks.",
                MagicStage.APPRENTICE, 3, 25, 60);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Reach up to 12 blocks." : "Reach up to 16 blocks.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double range = tier == 1 ? 8.0 : tier == 2 ? 12.0 : 16.0;

        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        // Stop just short of whatever we hit, and convert eye height to feet height
        Vec3 target = hit.getLocation().subtract(look.scale(0.6)).subtract(0, player.getEyeHeight(), 0);

        if (!level.noCollision(player, player.getBoundingBox().move(target.subtract(player.position())))) {
            target = target.add(0, 1, 0);
            if (!level.noCollision(player, player.getBoundingBox().move(target.subtract(player.position())))) {
                player.displayClientMessage(Component.translatable("hollowmancy.blink_spell.something_blocks_the_way")
                        .withStyle(ChatFormatting.GRAY), true);
                return false;
            }
        }

        Vec3 from = player.position();
        level.sendParticles(HollowmancyParticles.ARCANE_SPARK.get(), from.x, from.y + 1.0, from.z, 40, 0.3, 0.6, 0.3, 0.2);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.4f);

        player.teleportTo(target.x, target.y, target.z);
        player.resetFallDistance();
        SpellFx.beam(level, from.add(0, 1.0, 0), target.add(0, 1.0, 0), SpellFx.ARCANE, 0.25F, 8, SpellFx.SMOOTH);
        SpellFx.ring(level, HollowmancyParticles.RING_ARCANE.get(), target.add(0, 0.08, 0), 1.6F);

        level.sendParticles(HollowmancyParticles.ARCANE_SPARK.get(), target.x, target.y + 1.0, target.z, 40, 0.3, 0.6, 0.3, 0.05);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.4f);
        return true;
    }
}