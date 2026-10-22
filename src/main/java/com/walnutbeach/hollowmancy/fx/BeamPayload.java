package com.walnutbeach.hollowmancy.fx;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Tells nearby players to draw a beam. Sent by SpellFx.beam. */
public record BeamPayload(Vec3 from, Vec3 to, int color, float width, int life, int style) implements CustomPacketPayload {
    public static final Type<BeamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "beam"));
    public static final StreamCodec<FriendlyByteBuf, BeamPayload> CODEC = StreamCodec.ofMember(BeamPayload::write, BeamPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeDouble(from.x); buf.writeDouble(from.y); buf.writeDouble(from.z);
        buf.writeDouble(to.x); buf.writeDouble(to.y); buf.writeDouble(to.z);
        buf.writeInt(color);
        buf.writeFloat(width);
        buf.writeVarInt(life);
        buf.writeVarInt(style);
    }

    private static BeamPayload read(FriendlyByteBuf buf) {
        Vec3 from = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 to = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new BeamPayload(from, to, buf.readInt(), buf.readFloat(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(TYPE, CODEC, (payload, context) -> {
                if (FMLEnvironment.dist == Dist.CLIENT) context.enqueueWork(() -> com.walnutbeach.hollowmancy.client.fx.BeamRenderer.add(payload));
            });
        }
    }
}
