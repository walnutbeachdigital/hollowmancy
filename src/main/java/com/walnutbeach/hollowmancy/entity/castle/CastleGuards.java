package com.walnutbeach.hollowmancy.entity.castle;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Morvane's castle guard. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CastleGuards {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Hollowmancy.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RevenantGuard>> REVENANT_GUARD = ENTITY_TYPES.register("revenant_guard",
            () -> EntityType.Builder.of(RevenantGuard::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F)
                    .ridingOffset(-0.7F).clientTrackingRange(8).build("revenant_guard"));

    public static final DeferredItem<DeferredSpawnEggItem> REVENANT_GUARD_SPAWN_EGG = ITEMS.register("revenant_guard_spawn_egg",
            () -> new DeferredSpawnEggItem(REVENANT_GUARD, 0xA8A0B4, 0x50246C, new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(REVENANT_GUARD.get(), RevenantGuard.createAttributes().build());
    }

    /** They spawn inside the castle (its spawn list), on solid ground, light or dark. */
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(REVENANT_GUARD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private CastleGuards() {}
}
