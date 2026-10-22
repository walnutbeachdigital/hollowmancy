package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class FamiliarSetup {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OWL_FAMILIAR.get(), FamiliarEntity.createAttributes().build());
        event.put(ModEntities.WISP_FAMILIAR.get(), FamiliarEntity.createAttributes().build());
        event.put(ModEntities.GOLEM_FAMILIAR.get(), FamiliarEntity.createAttributes().build());
    }
}
