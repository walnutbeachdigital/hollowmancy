package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CastSpellPayload(String spellId) implements CustomPacketPayload {

    public static final Type<CastSpellPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "cast_spell"));

    public static final StreamCodec<ByteBuf, CastSpellPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(CastSpellPayload::new, CastSpellPayload::spellId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}