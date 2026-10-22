package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.HollowSovereignModel;
import com.walnutbeach.hollowmancy.entity.HollowSovereign;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.resources.ResourceLocation;

/** The Hollow Sovereign: a floating, hooded void-king with a crown and a halo of shards. */
public class SovereignRenderer extends MobRenderer<HollowSovereign, HollowSovereignModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/hollow_sovereign.png");
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 1.0F;

    public SovereignRenderer(EntityRendererProvider.Context context) {
        super(context, new HollowSovereignModel(context.bakeLayer(HollowSovereignModel.LAYER)), 0.8F);
        addLayer(new BossGlowLayer<>(this, "hollow_sovereign"));

    }

    @Override
    public ResourceLocation getTextureLocation(HollowSovereign entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(HollowSovereign entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
