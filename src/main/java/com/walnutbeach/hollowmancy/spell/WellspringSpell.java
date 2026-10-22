package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class WellspringSpell extends Spell {

    public WellspringSpell() {
        super("wellspring", "Wellspring", "Heal every friend within 8 blocks: players, animals, pets and villagers. You mend a little too.",
                MagicSchool.NATURE, MagicStage.ARCANE_ARTS, 5, 40, 300);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Reaches 10 blocks and heals 4 hearts."
                : "Reaches 12 blocks, heals 5 hearts, and cures poison.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double radius = tier == 1 ? 8.0 : tier == 2 ? 10.0 : 12.0;
        float heal = tier == 1 ? 6.0f : tier == 2 ? 8.0f : 10.0f;

        ServerLevel level = player.serverLevel();
        List<LivingEntity> friends = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive() && isFriend(entity)
                        && (entity.getHealth() < entity.getMaxHealth() || (tier >= 3 && entity.hasEffect(MobEffects.POISON))));
        boolean selfHurt = player.getHealth() < player.getMaxHealth();

        if (friends.isEmpty() && !selfHurt) {
            player.displayClientMessage(Component.translatable("hollowmancy.wellspring_spell.no_one_here_needs_mending")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        for (LivingEntity friend : friends) {
            friend.heal(heal);
            friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            if (tier >= 3) friend.removeEffect(MobEffects.POISON);
            level.sendParticles(HollowmancyParticles.HEAL_MOTE.get(), friend.getX(), friend.getY() + friend.getBbHeight() + 0.3, friend.getZ(),
                    3, 0.3, 0.2, 0.3, 0.0);
        }
        player.heal(heal / 2.0f);
        if (tier >= 3) player.removeEffect(MobEffects.POISON);

        level.sendParticles(HollowmancyParticles.WATER_MOTE.get(), player.getX(), player.getY() + 0.2, player.getZ(),
                60, radius / 3, 0.1, radius / 3, 0.2);
        level.sendParticles(HollowmancyParticles.HEAL_MOTE.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                20, radius / 3, 0.6, radius / 3, 0.0);
        SpellFx.flourish(level, getSchool(), player.position(), (float) radius);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.2f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
        return true;
    }

    /** Anything that isn't a monster and counts as a friend. */
    private static boolean isFriend(LivingEntity entity) {
        if (entity instanceof Enemy) return false; // hoglins are animals but still hostile
        return entity instanceof Player
                || entity instanceof Animal
                || entity instanceof AbstractVillager
                || entity instanceof IronGolem;
    }
}