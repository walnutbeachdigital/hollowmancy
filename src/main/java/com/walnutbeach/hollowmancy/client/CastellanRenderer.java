package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.CastellanModel;
import com.walnutbeach.hollowmancy.entity.Castellan;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/** The Castellan: mail, an iron barrel helm, the treasury chain, axe and shield. */
public class CastellanRenderer extends MobRenderer<Castellan, CastellanModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/castellan.png");
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 1.0F;

    public CastellanRenderer(EntityRendererProvider.Context context) {
        super(context, new CastellanModel(context.bakeLayer(CastellanModel.LAYER)), 0.7F);
        addLayer(new BossGlowLayer<>(this, "castellan"));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Castellan entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Castellan entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
