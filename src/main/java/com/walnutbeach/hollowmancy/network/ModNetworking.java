package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.ClientSpellState;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Server -> client
        registrar.playToClient(SyncMagicPayload.TYPE, SyncMagicPayload.STREAM_CODEC, ModNetworking::handleSync);
        registrar.playToClient(SpellCooldownPayload.TYPE, SpellCooldownPayload.STREAM_CODEC, ModNetworking::handleCooldown);
        registrar.playToClient(ChannelPayload.TYPE, ChannelPayload.STREAM_CODEC, ModNetworking::handleChannel);

        // Client -> server
        registrar.playToServer(CastSpellPayload.TYPE, CastSpellPayload.STREAM_CODEC, ModNetworking::handleCast);
        registrar.playToServer(LearnSpellPayload.TYPE, LearnSpellPayload.STREAM_CODEC, ModNetworking::handleLearn);
    }

    private static void handleSync(SyncMagicPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> context.player().setData(ModAttachments.MAGIC, payload.data()));
    }

    private static void handleCooldown(SpellCooldownPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSpellState.setCooldown(
                payload.spellId(), payload.remaining(), payload.total(), context.player().level().getGameTime()));
    }

    private static void handleChannel(ChannelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSpellState.setChannel(
                payload.spellId(), payload.ticks(), context.player().level().getGameTime()));
    }

    private static void handleCast(CastSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) Spells.tryCast(player, payload.spellId());
        });
    }

    private static void handleLearn(LearnSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) Spells.tryLearn(player, payload.spellId());
        });
    }
}