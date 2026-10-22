package com.walnutbeach.hollowmancy.ending;

import com.walnutbeach.hollowmancy.Hollowmancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Tells a player's game to roll the credits. */
public record CreditsPayload() implements CustomPacketPayload {

    public static final CreditsPayload INSTANCE = new CreditsPayload();
    public static final Type<CreditsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "credits"));
    public static final StreamCodec<ByteBuf, CreditsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                if (FMLEnvironment.dist.isClient()) com.walnutbeach.hollowmancy.client.CreditsScreen.open();
            }));
        }
    }
}
