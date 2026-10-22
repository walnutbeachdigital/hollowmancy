package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncMagicPayload(MagicData data) implements CustomPacketPayload {

    public static final Type<SyncMagicPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "sync_magic"));

    public static final StreamCodec<ByteBuf, SyncMagicPayload> STREAM_CODEC =
            MagicData.STREAM_CODEC.map(SyncMagicPayload::new, SyncMagicPayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
