package com.walnutbeach.hollowmancy.network;

import com.walnutbeach.hollowmancy.Hollowmancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "this spell has `remaining` ticks of cooldown left, out of `total`." */
public record SpellCooldownPayload(String spellId, int remaining, int total) implements CustomPacketPayload {

    public static final Type<SpellCooldownPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "spell_cooldown"));

    public static final StreamCodec<ByteBuf, SpellCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellCooldownPayload::spellId,
            ByteBufCodecs.VAR_INT, SpellCooldownPayload::remaining,
            ByteBufCodecs.VAR_INT, SpellCooldownPayload::total,
            SpellCooldownPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}