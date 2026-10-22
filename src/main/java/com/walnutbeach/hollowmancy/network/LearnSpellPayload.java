package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LearnSpellPayload(String spellId) implements CustomPacketPayload {

    public static final Type<LearnSpellPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "learn_spell"));

    public static final StreamCodec<ByteBuf, LearnSpellPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(LearnSpellPayload::new, LearnSpellPayload::spellId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}