package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.archmage.ArchmageItems;
import com.walnutbeach.hollowmancy.item.WandMastery;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** The Archmage's Staff changes its look with mastery, like the other wands. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArchmageClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ArchmageItems.ARCHMAGE_STAFF.get(), WandMasteryClient.MASTERY,
                (stack, level, entity, seed) -> WandMastery.level(stack) / (float) WandMastery.MAX_LEVEL));
    }
}
