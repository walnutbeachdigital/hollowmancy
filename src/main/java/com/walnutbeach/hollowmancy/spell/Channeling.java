package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.network.ChannelPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Channeling {

    private static final double MAX_DRIFT_SQR = 1.5 * 1.5; // move further than this and the spell breaks

    private record Channel(Spell spell, long startTick, int duration, Vec3 startPos) {}

    private static final Map<UUID, Channel> ACTIVE = new HashMap<>();

    public static boolean isChanneling(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void start(ServerPlayer player, Spell spell, int duration) {
        ServerLevel level = player.serverLevel();
        ACTIVE.put(player.getUUID(), new Channel(spell, level.getGameTime(), duration, player.position()));
        PacketDistributor.sendToPlayer(player, new ChannelPayload(spell.getId(), duration));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    /** Stop channeling. Nothing is paid. Pass null for a silent cancel. */
    public static void cancel(ServerPlayer player, String message) {
        if (ACTIVE.remove(player.getUUID()) == null) return;
        PacketDistributor.sendToPlayer(player, ChannelPayload.ENDED);
        if (message != null) {
            player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GRAY), true);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4f, 1.6f);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Channel channel = ACTIVE.get(player.getUUID());
        if (channel == null) return;

        if (!player.isAlive()) {
            cancel(player, null);
            return;
        }
        if (player.position().distanceToSqr(channel.startPos()) > MAX_DRIFT_SQR) {
            cancel(player, "You moved, and the spell slips away.");
            return;
        }

        ServerLevel level = player.serverLevel();
        long elapsed = level.getGameTime() - channel.startTick();
        float progress = Math.min(1.0f, (float) elapsed / channel.duration());

        // Rooted in place while channeling
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 4, false, false));

        // A swirl in the school's color, tightening and rising as it builds
        int color = channel.spell().getSchool().getColor();
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f), 1.0f);
        double spin = elapsed * 0.35;
        double radius = 1.3 - 0.7 * progress;
        double height = 0.2 + 1.8 * progress;
        for (int i = 0; i < 3; i++) {
            double angle = spin + i * (Math.PI * 2 / 3);
            level.sendParticles(SpellFx.spark(channel.spell().getSchool()),
                    player.getX() + Math.cos(angle) * radius, player.getY() + height, player.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
        if (elapsed % 4 == 0) {
            SpellFx.runeCircle(level, SpellFx.rune(channel.spell().getSchool()), player.position().add(0, 0.15, 0), 1.1 - 0.5 * progress, 2);
        }
        if (elapsed % 10 == 0) {
            SpellFx.ring(level, SpellFx.ringFor(channel.spell().getSchool()), player.position().add(0, 0.06, 0), 1.4F);
        }
        if (elapsed % 5 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 0.6f + progress);
        }

        // Release
        if (elapsed >= channel.duration()) {
            ACTIVE.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, ChannelPayload.ENDED);
            level.sendParticles(SpellFx.spark(channel.spell().getSchool()), player.getX(), player.getY() + 1.2, player.getZ(),
                    20, 0.3, 0.5, 0.3, 0.08);
            SpellFx.flourish(level, channel.spell().getSchool(), player.position(), 2.2F);
            Spells.release(player, channel.spell());
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Channel channel = ACTIVE.get(player.getUUID());
        if (channel == null || event.getAmount() <= 0) return;
        if (channel.spell().isInterruptedByDamage()) {
            cancel(player, "The blow breaks your concentration.");
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player, null);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }
}