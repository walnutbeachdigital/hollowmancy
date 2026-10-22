package com.walnutbeach.hollowmancy.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

/**
 * Random spread placement whose spacing follows the "landmarkFrequency" setting. Structure sets use it as
 * "type": "hollowmancy:config_spread" with the usual spacing / separation / salt.
 */
public class ConfiguredSpreadPlacement extends RandomSpreadStructurePlacement {
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Hollowmancy.MODID);

    public static final MapCodec<ConfiguredSpreadPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> placementCodec(instance).and(instance.group(
            Codec.intRange(0, 4096).fieldOf("spacing").forGetter(p -> p.baseSpacing),
            Codec.intRange(0, 4096).fieldOf("separation").forGetter(p -> p.baseSeparation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType)
    )).apply(instance, ConfiguredSpreadPlacement::new));

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ConfiguredSpreadPlacement>> TYPE =
            PLACEMENTS.register("config_spread", () -> () -> CODEC);

    private final int baseSpacing, baseSeparation;

    public ConfiguredSpreadPlacement(Vec3i locateOffset, FrequencyReductionMethod method, float frequency, int salt,
                                     Optional<ExclusionZone> exclusionZone, int spacing, int separation, RandomSpreadType spreadType) {
        super(locateOffset, method, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.baseSpacing = spacing;
        this.baseSeparation = separation;
    }

    public static void register(IEventBus modEventBus) {
        PLACEMENTS.register(modEventBus);
    }

    private static double scale() {
        return 1.0 / Math.sqrt(HollowmancyExtraConfig.get(HollowmancyExtraConfig.LANDMARK_FREQUENCY));
    }

    @Override
    public int spacing() {
        return Math.max(2, (int) Math.round(baseSpacing * scale()));
    }

    @Override
    public int separation() {
        return Math.max(1, Math.min(spacing() - 1, (int) Math.round(baseSeparation * scale())));
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int regionX, int regionZ) {
        int spacing = spacing(), separation = separation();
        int cellX = Math.floorDiv(regionX, spacing), cellZ = Math.floorDiv(regionZ, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, cellX, cellZ, salt());
        int range = spacing - separation;
        int x = spreadType().evaluate(random, range), z = spreadType().evaluate(random, range);
        return new ChunkPos(cellX * spacing + x, cellZ * spacing + z);
    }

    @Override
    public StructurePlacementType<?> type() {
        return TYPE.get();
    }
}
