package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Familiars from the Hollow's own creatures: the Crystal Moth and the Glimmerhorn. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class HollowFamiliars {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Hollowmancy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MothFamiliar>> MOTH_FAMILIAR = ENTITY_TYPES.register("moth_familiar",
            () -> EntityType.Builder.of(MothFamiliar::new, MobCategory.CREATURE).sized(0.6F, 0.4F).eyeHeight(0.2F)
                    .clientTrackingRange(10).build("moth_familiar"));
    public static final DeferredHolder<EntityType<?>, EntityType<GlimmerhornFamiliar>> GLIMMERHORN_FAMILIAR = ENTITY_TYPES.register("glimmerhorn_familiar",
            () -> EntityType.Builder.of(GlimmerhornFamiliar::new, MobCategory.CREATURE).sized(0.9F, 1.5F).eyeHeight(1.35F)
                    .passengerAttachments(1.2F).clientTrackingRange(10).build("glimmerhorn_familiar"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(MOTH_FAMILIAR.get(), FamiliarEntity.createAttributes().build());
        event.put(GLIMMERHORN_FAMILIAR.get(), FamiliarEntity.createAttributes().build());
    }

    private HollowFamiliars() {}
}
