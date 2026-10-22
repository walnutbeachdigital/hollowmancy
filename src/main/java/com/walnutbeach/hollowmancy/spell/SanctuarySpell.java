package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class SanctuarySpell extends Spell {

    public SanctuarySpell() {
        super("sanctuary", "Sanctuary", "You and every player within 8 blocks regenerate and resist fire. Nearby enemies are weakened.",
                MagicSchool.WARDING, MagicStage.MASTERY, 7, 70, 900);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Reaches 10 blocks and lasts 25 seconds."
                : "Reaches 12 blocks, lasts 30 seconds, and grants Regeneration III.";
    }

    @Override
    public int getChargeTicks() {
        return 30; // 1.5 seconds
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 8.0 : tier == 2 ? 10.0 : 12.0;
        int duration = tier == 1 ? 400 : tier == 2 ? 500 : 600;
        int regenLevel = tier == 3 ? 2 : 1;

        ServerLevel level = player.serverLevel();

        List<Player> allies = level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius));
        for (Player ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, regenLevel));
            ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
            level.sendParticles(HollowmancyParticles.HEAL_MOTE.get(), ally.getX(), ally.getY() + 2.0, ally.getZ(),
                    4, 0.3, 0.2, 0.3, 0.0);
        }

        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity instanceof Enemy && entity.isAlive());
        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), enemy.getX(), enemy.getY() + 1.0, enemy.getZ(),
                    8, 0.3, 0.4, 0.3, 0.01);
        }

        level.sendParticles(HollowmancyParticles.WARD_GLINT.get(), player.getX(), player.getY() + 0.2, player.getZ(),
                60, radius / 2, 0.1, radius / 2, 0.01);
        SpellFx.ring(level, HollowmancyParticles.RING_WARD.get(), player.position().add(0, 0.08, 0), (float) radius);
        SpellFx.runeCircle(level, HollowmancyParticles.RUNE_WARD.get(), player.position().add(0, 0.2, 0), radius * 0.5, 8);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.4f);
        return true;
    }
}