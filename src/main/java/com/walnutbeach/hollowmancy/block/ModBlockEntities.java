package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Hollowmancy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaBatteryBlockEntity>> MANA_BATTERY =
            BLOCK_ENTITIES.register("mana_battery",
                    () -> BlockEntityType.Builder.of(ManaBatteryBlockEntity::new, Hollowmancy.MANA_BATTERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WardingLanternBlockEntity>> WARDING_LANTERN =
            BLOCK_ENTITIES.register("warding_lantern",
                    () -> BlockEntityType.Builder.of(WardingLanternBlockEntity::new, Hollowmancy.WARDING_LANTERN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VerdantEngineBlockEntity>> VERDANT_ENGINE =
            BLOCK_ENTITIES.register("verdant_engine",
                    () -> BlockEntityType.Builder.of(VerdantEngineBlockEntity::new, Hollowmancy.VERDANT_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchEngineBlockEntity>> RESEARCH_ENGINE =
            BLOCK_ENTITIES.register("research_engine",
                    () -> BlockEntityType.Builder.of(ResearchEngineBlockEntity::new, Hollowmancy.RESEARCH_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaBeaconBlockEntity>> MANA_BEACON =
        BLOCK_ENTITIES.register("mana_beacon",
            () -> BlockEntityType.Builder.of(ManaBeaconBlockEntity::new, Hollowmancy.MANA_BEACON.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfuserBlockEntity>> INFUSER =
        BLOCK_ENTITIES.register("infuser",
            () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, Hollowmancy.INFUSER.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }

    // Lets hoppers (and other mods' pipes) put Dull Shards into the Research Engine
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RESEARCH_ENGINE.get(),
                (engine, side) -> engine.getShardHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, INFUSER.get(),
            (infuser, side) -> infuser.getHandler(side));
    }
}