package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Soaring {

    /** Saved on the player: present while Soar is active, holding the spell's tier. */
    private static final String KEY = "hollowmancy_soaring";
    private static final float NORMAL_SPEED = 0.05f;   // vanilla creative flying speed
    private static final float FAST_SPEED = 0.07f;     // Soar III
    private static final int WARNING_SECONDS = 10;

    public static boolean isSoaring(Player player) {
        return player.getPersistentData().contains(KEY);
    }

    public static int drainPerSecond(int tier) {
        return tier == 1 ? 3 : tier == 2 ? 2 : 1;
    }

    public static void start(ServerPlayer player, int tier) {
        player.getPersistentData().putInt(KEY, tier);
        Abilities abilities = player.getAbilities();
        abilities.mayfly = true;
        abilities.setFlyingSpeed(tier >= 3 ? FAST_SPEED : NORMAL_SPEED);
        player.onUpdateAbilities();

        player.displayClientMessage(Component.translatable("hollowmancy.soaring.wind_gathers_beneath_you_double_tap")
                .withStyle(ChatFormatting.AQUA), true);
        player.serverLevel().sendParticles(HollowmancyParticles.WIND.get(), player.getX(), player.getY() + 0.2, player.getZ(),
                30, 0.6, 0.1, 0.6, 0.08);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0f, 1.3f);
    }

    /** End Soar. If the player was in the air, they drift down under Slow Falling. */
    public static void stop(ServerPlayer player, String message) {
        if (!isSoaring(player)) return;
        player.getPersistentData().remove(KEY);

        if (!player.isCreative() && !player.isSpectator()) {
            Abilities abilities = player.getAbilities();
            boolean airborne = abilities.flying || !player.onGround();
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(NORMAL_SPEED);
            player.onUpdateAbilities();
            if (airborne) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        }

        if (message != null) {
            player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GRAY), true);
        }
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.6f, 0.8f);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isSoaring(player)) return;

        // Creative and spectator have their own flight: step aside
        if (player.isCreative() || player.isSpectator()) {
            player.getPersistentData().remove(KEY);
            return;
        }

        // Put flight back if something reset it (rejoining, changing dimension)
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }

        if (!abilities.flying) return; // walking costs nothing

        // A light trail
        if (player.tickCount % 4 == 0) {
            player.serverLevel().sendParticles(HollowmancyParticles.WIND.get(), player.getX(), player.getY(), player.getZ(),
                    1, 0.2, 0.05, 0.2, 0.0);
        }

        if (player.tickCount % 20 != 0) return; // drain once per second

        int drain = drainPerSecond(player.getPersistentData().getInt(KEY));
        MagicData data = MagicHelper.get(player);
        if (data.mana() < drain) {
            stop(player, "Your mana runs dry. The wind lets you down gently.");
            return;
        }
        MagicHelper.set(player, data.withMana(data.mana() - drain));

        int secondsLeft = (data.mana() - drain) / drain;
        if (secondsLeft <= WARNING_SECONDS && secondsLeft % 5 == 0) {
            player.displayClientMessage(Component.translatable("hollowmancy.soaring.your_wings_falter_s_of_flight", secondsLeft)
                    .withStyle(ChatFormatting.GOLD), true);
        }
    }

    // Flight doesn't survive death
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stop(player, null);
    }
}