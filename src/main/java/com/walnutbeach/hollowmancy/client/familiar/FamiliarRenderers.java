package com.walnutbeach.hollowmancy.client.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import com.walnutbeach.hollowmancy.familiar.GolemFamiliar;
import com.walnutbeach.hollowmancy.familiar.OwlFamiliar;
import com.walnutbeach.hollowmancy.familiar.WispFamiliar;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FamiliarRenderers {

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/familiar/" + name + ".png");
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OwlModel.LAYER, OwlModel::createBodyLayer);
        event.registerLayerDefinition(WispModel.LAYER, WispModel::createBodyLayer);
        event.registerLayerDefinition(GolemModel.LAYER, GolemModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.OWL_FAMILIAR.get(), OwlRenderer::new);
        event.registerEntityRenderer(ModEntities.WISP_FAMILIAR.get(), WispRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_FAMILIAR.get(), GolemRenderer::new);
    }

    public static class OwlRenderer extends MobRenderer<OwlFamiliar, OwlModel> {
        private static final ResourceLocation TEXTURE = texture("owl");

        public OwlRenderer(EntityRendererProvider.Context context) {
            super(context, new OwlModel(context.bakeLayer(OwlModel.LAYER)), 0.25f);
        }

        @Override
        public ResourceLocation getTextureLocation(OwlFamiliar entity) {
            return TEXTURE;
        }
    }

    public static class WispRenderer extends MobRenderer<WispFamiliar, WispModel> {
        private static final ResourceLocation TEXTURE = texture("wisp");

        public WispRenderer(EntityRendererProvider.Context context) {
            super(context, new WispModel(context.bakeLayer(WispModel.LAYER)), 0.0f);
        }

        @Override
        public ResourceLocation getTextureLocation(WispFamiliar entity) {
            return TEXTURE;
        }

        @Override
        protected int getBlockLightLevel(WispFamiliar entity, BlockPos pos) {
            return 15;   // it glows
        }
    }

    public static class GolemRenderer extends MobRenderer<GolemFamiliar, GolemModel> {
        private static final ResourceLocation TEXTURE = texture("golem");
        private static final RenderType EYES = RenderType.eyes(texture("golem_eyes"));

        public GolemRenderer(EntityRendererProvider.Context context) {
            super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER)), 0.45f);
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return EYES;
                }
            });
        }

        @Override
        public ResourceLocation getTextureLocation(GolemFamiliar entity) {
            return TEXTURE;
        }
    }
}
