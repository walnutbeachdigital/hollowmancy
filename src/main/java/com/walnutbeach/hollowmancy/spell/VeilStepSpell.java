package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import net.minecraft.world.phys.Vec3;

public class VeilStepSpell extends Spell {

    public static final ResourceKey<Level> HOLLOW =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "the_hollow"));

    public VeilStepSpell() {
        super("veil_step", "Veil Step", "Tear through the veil into the Hollow, or back out again. (+10 corruption)",
                MagicSchool.FORBIDDEN, MagicStage.ASCENSION, 6, 80, 200);
    }

    @Override
    public int getCorruption() {
        return 10;
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Arrive with Slow Falling, safe from the drop."
                : "Also carries every player within 3 blocks through with you.";
    }

    @Override
    public int getChargeTicks() {
        return 40; // 2 seconds
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

        boolean inHollow = player.level().dimension().equals(HOLLOW);
        ServerLevel target = inHollow ? server.overworld() : server.getLevel(HOLLOW);
        if (target == null) {
            player.displayClientMessage(Component.translatable("hollowmancy.veil_step_spell.the_hollow_does_not_answer_dimension")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        if (y <= target.getMinBuildHeight() + 1) {
            y = 70;
            Block platform = inHollow ? Blocks.STONE : Blocks.CRYING_OBSIDIAN;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    target.setBlock(new BlockPos(x + dx, y - 1, z + dz), platform.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        ServerLevel from = player.serverLevel();

        // Tier III: gather nearby players before leaving
        List<ServerPlayer> companions = tier >= 3
                ? from.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(3.0), other -> other != player)
                : List.of();

        from.sendParticles(HollowmancyParticles.VOID_WISP.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                60, 0.4, 0.8, 0.4, 0.1);
        SpellFx.ring(from, HollowmancyParticles.RING_VOID.get(), player.position().add(0, 0.08, 0), 2.4F);
        from.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.3f, 1.5f);

        player.teleportTo(target, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        if (tier >= 2) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));

        for (ServerPlayer companion : companions) {
            companion.teleportTo(target, x + 0.5, y, z + 0.5, companion.getYRot(), companion.getXRot());
            companion.resetFallDistance();
            companion.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            companion.displayClientMessage(Component.translatable("hollowmancy.veil_step_spell.pulls_you_through_the_veil", player.getName().getString())
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        }

        target.sendParticles(HollowmancyParticles.VOID_WISP.get(), x + 0.5, y + 1.0, z + 0.5, 60, 0.4, 0.8, 0.4, 0.1);
        SpellFx.ring(target, HollowmancyParticles.RING_VOID.get(), new Vec3(x + 0.5, y + 0.08, z + 0.5), 2.4F);
        player.displayClientMessage((inHollow ? Component.translatable("hollowmancy.veil_step_spell.you_tear_free_of_the_hollow") : Component.translatable("hollowmancy.veil_step_spell.the_veil_parts_you_are_in"))
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        return true;
    }
}