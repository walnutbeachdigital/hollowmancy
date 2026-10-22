package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.BossModelLayers;
import com.walnutbeach.hollowmancy.client.model.ListenerModel;
import com.walnutbeach.hollowmancy.entity.Listener;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The Listener: three hooded heads that hear everything, a caged void heart, long arms and trailing tendrils. */
public class ListenerRenderer extends MobRenderer<Listener, ListenerModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/listener.png");
    private static final ResourceLocation SHELL =
        ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_armor.png");
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 1.5F;

    public ListenerRenderer(EntityRendererProvider.Context context) {
        super(context, new ListenerModel(context.bakeLayer(ListenerModel.LAYER)), 1.2F);
        addLayer(new BossGlowLayer<>(this, "listener"));
        ListenerModel shell = new ListenerModel(context.bakeLayer(BossModelLayers.LISTENER_SHELL));
        // Below half health it crackles with a shell, like a wither's
        addLayer(new EnergySwirlLayer<Listener, ListenerModel>(this) {
            @Override
            protected float xOffset(float tickCount) {
                return Mth.cos(tickCount * 0.02F) * 3.0F;
            }

            @Override
            protected ResourceLocation getTextureLocation() {
                return SHELL;
            }

            @Override
            protected EntityModel<Listener> model() {
                return shell;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Listener entity) {
        return TEXTURE;
    }

    /** Grows into its full size while it wakes, the way a wither does. */
    @Override
    protected void scale(Listener entity, PoseStack poseStack, float partialTick) {
        float size = SCALE;
        int waking = entity.getInvulnerableTicks();
        if (waking > 0) size -= (waking - partialTick) / 220.0F * 0.5F;
        poseStack.scale(size, size, size);
    }
}
