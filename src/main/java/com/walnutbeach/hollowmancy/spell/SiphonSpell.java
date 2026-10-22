package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class SiphonSpell extends Spell {

    public SiphonSpell() {
        super("siphon", "Siphon", "Drain the life of every creature within 6 blocks into yourself. (+5 corruption)",
                MagicSchool.FORBIDDEN, MagicStage.ASCENSION, 5, 30, 60);
    }

    @Override
    public int getCorruption() {
        return 5;
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Reaches 7 blocks and heals more per creature."
                : "Reaches 8 blocks, drains harder, and heals much more.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 6.0 : tier == 2 ? 7.0 : 8.0;
        float damage = tier == 3 ? 4.0f : 3.0f;
        float healPerTarget = tier == 1 ? 1.5f : tier == 2 ? 2.0f : 3.0f;

        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive() && !(entity instanceof Player));

        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("hollowmancy.siphon_spell.there_is_nothing_here_to_drain")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().indirectMagic(player, player), damage);
            SpellFx.beam(level, target.position().add(0, target.getBbHeight() / 2, 0), player.position().add(0, 1.0, 0), SpellFx.VOID, 0.09F, 14, SpellFx.SMOOTH);
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    6, 0.2, 0.3, 0.2, 0.02);
        }
        player.heal(healPerTarget * targets.size());

        level.sendParticles(HollowmancyParticles.RUNE_VOID.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                targets.size() * 2, 0.4, 0.5, 0.4, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0f, 0.6f);
        return true;
    }
}