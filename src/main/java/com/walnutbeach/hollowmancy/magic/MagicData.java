package com.walnutbeach.hollowmancy.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public record MagicData(int stage, int mana, int maxMana, int knowledge, List<String> learned, int corruption) {

    public static final int MAX_CORRUPTION = 100;

    public static final MagicData DEFAULT = new MagicData(MagicStage.MUNDANE, 0, 100, 0, List.of(), 0);

    public static final Codec<MagicData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("stage").forGetter(MagicData::stage),
            Codec.INT.fieldOf("mana").forGetter(MagicData::mana),
            Codec.INT.fieldOf("max_mana").forGetter(MagicData::maxMana),
            Codec.INT.optionalFieldOf("knowledge", 0).forGetter(MagicData::knowledge),
            Codec.STRING.listOf().optionalFieldOf("learned", List.of()).forGetter(MagicData::learned),
            Codec.INT.optionalFieldOf("corruption", 0).forGetter(MagicData::corruption)
    ).apply(instance, MagicData::new));

    public static final StreamCodec<ByteBuf, MagicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MagicData::stage,
            ByteBufCodecs.VAR_INT, MagicData::mana,
            ByteBufCodecs.VAR_INT, MagicData::maxMana,
            ByteBufCodecs.VAR_INT, MagicData::knowledge,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MagicData::learned,
            ByteBufCodecs.VAR_INT, MagicData::corruption,
            MagicData::new
    );

    public MagicData withStage(int newStage) {
        return new MagicData(Mth.clamp(newStage, 1, 7), mana, maxMana, knowledge, learned, corruption);
    }

    public MagicData withMana(int newMana) {
        return new MagicData(stage, Mth.clamp(newMana, 0, maxMana), maxMana, knowledge, learned, corruption);
    }

    public MagicData withMaxMana(int newMaxMana) {
        int max = Math.max(1, newMaxMana);
        return new MagicData(stage, Math.min(mana, max), max, knowledge, learned, corruption);
    }

    public MagicData withKnowledge(int newKnowledge) {
        return new MagicData(stage, mana, maxMana, Math.max(0, newKnowledge), learned, corruption);
    }

    public MagicData withCorruption(int newCorruption) {
        return new MagicData(stage, mana, maxMana, knowledge, learned, Mth.clamp(newCorruption, 0, MAX_CORRUPTION));
    }

    public MagicData withLearned(String spellId) {
        if (learned.contains(spellId)) return this;
        List<String> copy = new ArrayList<>(learned);
        copy.add(spellId);
        return new MagicData(stage, mana, maxMana, knowledge, List.copyOf(copy), corruption);
    }

    public MagicData clearLearned() {
        return new MagicData(stage, mana, maxMana, knowledge, List.of(), corruption);
    }
}