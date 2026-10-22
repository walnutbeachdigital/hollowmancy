package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Registers the shapes of the six boss models. Runs by itself; nothing else needs to call it. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BossModelLayers {
    /** The Listener's crackling shell below half health, a slightly puffed-out copy of its body. */
    public static final ModelLayerLocation LISTENER_SHELL =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "listener"), "shell");

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HollowSovereignModel.LAYER, HollowSovereignModel::createBodyLayer);
        event.registerLayerDefinition(AmethystColossusModel.LAYER, AmethystColossusModel::createBodyLayer);
        event.registerLayerDefinition(RevenantKingModel.LAYER, RevenantKingModel::createBodyLayer);
        event.registerLayerDefinition(CastellanModel.LAYER, CastellanModel::createBodyLayer);
        event.registerLayerDefinition(LeviathanModel.LAYER, LeviathanModel::createBodyLayer);
        event.registerLayerDefinition(ListenerModel.LAYER, ListenerModel::createBodyLayer);
        event.registerLayerDefinition(LISTENER_SHELL, () -> ListenerModel.createBodyLayer(0.5F));
    }
}
