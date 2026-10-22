package com.walnutbeach.hollowmancy.worldgen;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The Mirror Sea: its biome, its two features, and the water that holds you up. */
public class MirrorSea {

    public static final ResourceKey<Biome> BIOME = key("mirror_sea");
    public static final ResourceKey<Biome> GROVE = key("hollow_grove");

    /** The Hollow's biome tiles are 256 blocks square (checkerboard scale 4). */
    public static final int TILE = 256;

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, name));
    }

    /**
     * Is this column Mirror Sea? Asks the biome source directly, so it works for any position,
     * even chunks that haven't generated yet.
     */
    public static boolean isMirrorSea(WorldGenLevel level, int x, int z) {
        ServerChunkCache chunks = level.getLevel().getChunkSource();
        return chunks.getGenerator().getBiomeSource().getNoiseBiome(
            QuartPos.fromBlock(x), QuartPos.fromBlock(MirrorSeaFeature.SURFACE), QuartPos.fromBlock(z),
            chunks.randomState().sampler()).is(BIOME);
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.FEATURE, helper -> {
                helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "mirror_sea"), new MirrorSeaFeature());
                helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "mirror_shore"), new MirrorShoreFeature());
            });
        }
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID)
    public static class Swimming {
        // In the Mirror Sea's water you can breathe and see, so the reflections and ruins can be explored
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
            if (!player.isInWater() || !player.level().getBiome(player.blockPosition()).is(BIOME)) return;
            // ...unless the Leviathan is singing nearby
            if (!player.level().getEntitiesOfClass(com.walnutbeach.hollowmancy.entity.Leviathan.class,
                    player.getBoundingBox().inflate(com.walnutbeach.hollowmancy.entity.Leviathan.SONG_RADIUS)).isEmpty()) return;
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 100, 0, true, true));
        }
    }
}
