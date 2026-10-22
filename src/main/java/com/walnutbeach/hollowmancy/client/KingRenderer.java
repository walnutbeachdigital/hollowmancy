package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.RevenantKingModel;
import com.walnutbeach.hollowmancy.entity.RevenantKing;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/** Morvane, the Revenant King: netherite plate, a gold crown, a tattered royal cape and burning eyes. */
public class KingRenderer extends MobRenderer<RevenantKing, RevenantKingModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/revenant_king.png");
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 1.05F;

    public KingRenderer(EntityRendererProvider.Context context) {
        super(context, new RevenantKingModel(context.bakeLayer(RevenantKingModel.LAYER)), 0.9F);
        addLayer(new BossGlowLayer<>(this, "revenant_king"));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(RevenantKing entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RevenantKing entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
