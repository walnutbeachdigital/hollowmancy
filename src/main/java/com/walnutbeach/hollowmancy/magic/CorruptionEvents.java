package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.ArcanistGear;
import com.walnutbeach.hollowmancy.item.Artifacts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class CorruptionEvents {

    private static final String[] WHISPERS = {
            "...it sees you...",
            "...deeper...",
            "...the Hollow remembers your name...",
            "...give us more...",
            "...you were never meant to know this...",
            "...open the door..."
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return; // once per second

        MagicData data = MagicHelper.get(player);
        int corruption = data.corruption();
        if (corruption <= 0) return;

        RandomSource random = player.getRandom();

        // Slow natural decay: 1 point per minute
        if (player.tickCount % ArcanistGear.decayInterval(player, (HollowmancyConfig.CORRUPTION_DECAY_SECONDS.get() * 20)) == 0) {
            MagicHelper.set(player, data.withCorruption(corruption - 1));
        }

        // The Sovereign's Crown: corruption no longer harms its bearer
        if (Artifacts.hasCrown(player)) return;

        // 25+: whispers
        if (corruption >= 25 && random.nextInt(60) == 0) {
            String whisper = WHISPERS[random.nextInt(WHISPERS.length)];
            player.displayClientMessage(Component.literal(whisper)
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
            player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 1.0f, 0.8f);
        }

        // 50+: darkness
        if (corruption >= 50 && random.nextInt(40) == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
        }

        // 75+: the body begins to fail
        if (corruption >= 75 && random.nextInt(20) == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
        }
    }
}