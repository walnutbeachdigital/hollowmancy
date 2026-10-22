package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.AmethystColossusModel;
import com.walnutbeach.hollowmancy.entity.AmethystColossus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.resources.ResourceLocation;

/** The Amethyst Colossus: a hunched geode giant with crystal-studded fists and a glowing core. */
public class ColossusRenderer extends MobRenderer<AmethystColossus, AmethystColossusModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/amethyst_colossus.png");
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 1.3F;

    public ColossusRenderer(EntityRendererProvider.Context context) {
        super(context, new AmethystColossusModel(context.bakeLayer(AmethystColossusModel.LAYER)), 1.4F);
        addLayer(new BossGlowLayer<>(this, "amethyst_colossus"));

    }

    @Override
    public ResourceLocation getTextureLocation(AmethystColossus entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AmethystColossus entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
