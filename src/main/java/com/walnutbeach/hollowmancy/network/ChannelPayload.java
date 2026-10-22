package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "you started channeling this spell for `ticks`". An empty id / 0 ticks means the channel ended. */
public record ChannelPayload(String spellId, int ticks) implements CustomPacketPayload {

    public static final Type<ChannelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "channel"));

    public static final StreamCodec<ByteBuf, ChannelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ChannelPayload::spellId,
            ByteBufCodecs.VAR_INT, ChannelPayload::ticks,
            ChannelPayload::new
    );

    public static final ChannelPayload ENDED = new ChannelPayload("", 0);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}