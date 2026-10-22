package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.RevenantGuardModel;
import com.walnutbeach.hollowmancy.entity.castle.CastleGuards;
import com.walnutbeach.hollowmancy.entity.castle.RevenantGuard;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Revenant Guard: bones in a rusted helm, gorget and pauldron, a torn purple tabard, eyes that glow. */
public class RevenantGuardRenderer extends MobRenderer<RevenantGuard, RevenantGuardModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/revenant_guard.png");

    public RevenantGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new RevenantGuardModel(context.bakeLayer(RevenantGuardModel.LAYER)), 0.5F);
        addLayer(new BossGlowLayer<>(this, "revenant_guard"));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(RevenantGuard entity) {
        return TEXTURE;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(RevenantGuardModel.LAYER, RevenantGuardModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CastleGuards.REVENANT_GUARD.get(), RevenantGuardRenderer::new);
        }
    }
}
