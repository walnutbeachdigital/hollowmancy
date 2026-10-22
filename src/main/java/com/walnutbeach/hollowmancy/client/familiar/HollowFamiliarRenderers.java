package com.walnutbeach.hollowmancy.client.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.HollowCreatureRenderers;
import com.walnutbeach.hollowmancy.familiar.GlimmerhornFamiliar;
import com.walnutbeach.hollowmancy.familiar.HollowFamiliars;
import com.walnutbeach.hollowmancy.familiar.MothFamiliar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Hollow familiars look like their wild kin (same models and textures), with a little more glow. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HollowFamiliarRenderers {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MothFamiliarModel.LAYER, MothFamiliarModel::createBodyLayer);
        event.registerLayerDefinition(GlimmerhornFamiliarModel.LAYER, GlimmerhornFamiliarModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HollowFamiliars.MOTH_FAMILIAR.get(), ctx -> new HollowCreatureRenderers.Creature<MothFamiliar, MothFamiliarModel>(ctx,
                new MothFamiliarModel(ctx.bakeLayer(MothFamiliarModel.LAYER)), 0.15F, 1.0F, true, e -> "crystal_moth"));
        event.registerEntityRenderer(HollowFamiliars.GLIMMERHORN_FAMILIAR.get(), ctx -> new HollowCreatureRenderers.Creature<GlimmerhornFamiliar, GlimmerhornFamiliarModel>(ctx,
                new GlimmerhornFamiliarModel(ctx.bakeLayer(GlimmerhornFamiliarModel.LAYER)), 0.6F, 1.0F, false, e -> "glimmerhorn"));
    }
}
