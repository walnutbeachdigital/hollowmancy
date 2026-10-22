package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * The creatures that live in the Hollow: their types, stats, spawn rules and spawn eggs.
 * Where each one spawns (and how often) is set in the biome files: data/hollowmancy/worldgen/biome/*.json
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HollowCreatures {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Hollowmancy.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    // ---- Ambient life ----
    public static final DeferredHolder<EntityType<?>, EntityType<Wisp>> WISP = ENTITY_TYPES.register("wisp",
            () -> EntityType.Builder.of(Wisp::new, MobCategory.AMBIENT).sized(0.5F, 0.5F).eyeHeight(0.25F)
                    .clientTrackingRange(8).build("wisp"));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalMoth>> CRYSTAL_MOTH = ENTITY_TYPES.register("crystal_moth",
            () -> EntityType.Builder.of(CrystalMoth::new, MobCategory.AMBIENT).sized(0.6F, 0.4F).eyeHeight(0.2F)
                    .clientTrackingRange(8).build("crystal_moth"));
    public static final DeferredHolder<EntityType<?>, EntityType<MirrorMinnow>> MIRROR_MINNOW = ENTITY_TYPES.register("mirror_minnow",
            () -> EntityType.Builder.of(MirrorMinnow::new, MobCategory.WATER_AMBIENT).sized(0.4F, 0.3F).eyeHeight(0.15F)
                    .clientTrackingRange(4).build("mirror_minnow"));
    public static final DeferredHolder<EntityType<?>, EntityType<Glimmerhorn>> GLIMMERHORN = ENTITY_TYPES.register("glimmerhorn",
            () -> EntityType.Builder.of(Glimmerhorn::new, MobCategory.CREATURE).sized(0.9F, 1.5F).eyeHeight(1.35F)
                    .clientTrackingRange(10).build("glimmerhorn"));

    // ---- Hostile ----
    public static final DeferredHolder<EntityType<?>, EntityType<HollowShade>> HOLLOW_SHADE = ENTITY_TYPES.register("hollow_shade",
            () -> EntityType.Builder.of(HollowShade::new, MobCategory.MONSTER).sized(0.6F, 2.5F).eyeHeight(2.15F)
                    .clientTrackingRange(8).build("hollow_shade"));
    public static final DeferredHolder<EntityType<?>, EntityType<Shardling>> SHARDLING = ENTITY_TYPES.register("shardling",
            () -> EntityType.Builder.of(Shardling::new, MobCategory.MONSTER).sized(0.8F, 0.95F).eyeHeight(0.6F)
                    .clientTrackingRange(8).build("shardling"));
    public static final DeferredHolder<EntityType<?>, EntityType<MirrorLurker>> MIRROR_LURKER = ENTITY_TYPES.register("mirror_lurker",
            () -> EntityType.Builder.of(MirrorLurker::new, MobCategory.MONSTER).sized(0.9F, 0.6F).eyeHeight(0.3F)
                    .clientTrackingRange(8).build("mirror_lurker"));

    // ---- Spawn eggs (base colour, spots colour) ----
    public static final DeferredItem<DeferredSpawnEggItem> WISP_SPAWN_EGG = egg("wisp", WISP, 0xBFF6FF, 0x5FD6F0);
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_MOTH_SPAWN_EGG = egg("crystal_moth", CRYSTAL_MOTH, 0x3B2A58, 0xB494F0);
    public static final DeferredItem<DeferredSpawnEggItem> MIRROR_MINNOW_SPAWN_EGG = egg("mirror_minnow", MIRROR_MINNOW, 0xD8E6F0, 0x7AE8F4);
    public static final DeferredItem<DeferredSpawnEggItem> GLIMMERHORN_SPAWN_EGG = egg("glimmerhorn", GLIMMERHORN, 0x5A4A6A, 0x9EF0FF);
    public static final DeferredItem<DeferredSpawnEggItem> HOLLOW_SHADE_SPAWN_EGG = egg("hollow_shade", HOLLOW_SHADE, 0x140C22, 0xB080FF);
    public static final DeferredItem<DeferredSpawnEggItem> SHARDLING_SPAWN_EGG = egg("shardling", SHARDLING, 0x3E3450, 0xB47CFF);
    public static final DeferredItem<DeferredSpawnEggItem> MIRROR_LURKER_SPAWN_EGG = egg("mirror_lurker", MIRROR_LURKER, 0x9AB8CC, 0x6AE0F0);

    private static final List<DeferredItem<DeferredSpawnEggItem>> EGGS = List.of(WISP_SPAWN_EGG, CRYSTAL_MOTH_SPAWN_EGG,
            MIRROR_MINNOW_SPAWN_EGG, GLIMMERHORN_SPAWN_EGG, HOLLOW_SHADE_SPAWN_EGG, SHARDLING_SPAWN_EGG, MIRROR_LURKER_SPAWN_EGG);

    private static <T extends net.minecraft.world.entity.Mob> DeferredItem<DeferredSpawnEggItem> egg(
            String name, DeferredHolder<EntityType<?>, EntityType<T>> type, int base, int spots) {
        return ITEMS.register(name + "_spawn_egg", () -> new DeferredSpawnEggItem(type, base, spots, new Item.Properties()));
    }

    /** Called from Hollowmancy's constructor. */
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /** Puts the eggs in Hollowmancy's own creative tab too (called from the tab's item list). */
    public static void addToTab(CreativeModeTab.Output output) {
        EGGS.forEach(egg -> output.accept(egg.get()));
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WISP.get(), Wisp.createAttributes().build());
        event.put(CRYSTAL_MOTH.get(), CrystalMoth.createAttributes().build());
        event.put(MIRROR_MINNOW.get(), MirrorMinnow.createAttributes().build());
        event.put(GLIMMERHORN.get(), Glimmerhorn.createAttributes().build());
        event.put(HOLLOW_SHADE.get(), HollowShade.createAttributes().build());
        event.put(SHARDLING.get(), Shardling.createAttributes().build());
        event.put(MIRROR_LURKER.get(), MirrorLurker.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        RegisterSpawnPlacementsEvent.Operation replace = RegisterSpawnPlacementsEvent.Operation.REPLACE;
        event.register(WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HollowFlyer::checkFlyerSpawnRules, replace);
        event.register(CRYSTAL_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HollowFlyer::checkFlyerSpawnRules, replace);
        event.register(MIRROR_MINNOW.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MirrorMinnow::checkMinnowSpawnRules, replace);
        event.register(GLIMMERHORN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Glimmerhorn::checkGlimmerhornSpawnRules, replace);
        event.register(HOLLOW_SHADE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, replace);
        event.register(SHARDLING.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, replace);
        event.register(MIRROR_LURKER.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR,
                MirrorLurker::checkLurkerSpawnRules, replace);
    }

    @SubscribeEvent
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) EGGS.forEach(egg -> event.accept(egg.get()));
    }
}
