package com.walnutbeach.hollowmancy.item;

import com.mojang.serialization.Codec;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Hollowmancy.MODID);

    /** How much mana a Mana Crystal is holding. Saved with the item and synced to clients. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_MANA =
            COMPONENTS.register("stored_mana", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}