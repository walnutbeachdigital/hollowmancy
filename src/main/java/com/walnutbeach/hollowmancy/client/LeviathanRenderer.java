package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.BossGlowLayer;
import com.walnutbeach.hollowmancy.client.model.LeviathanModel;
import com.walnutbeach.hollowmancy.entity.Leviathan;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** The Leviathan: a long-tailed abyssal beast with one great eye, a hinged jaw and bristling spines. Draws its own laser. */
public class LeviathanRenderer extends MobRenderer<Leviathan, LeviathanModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/leviathan.png");
    private static final RenderType BEAM =
        RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png"));
    /** Change this to make it bigger or smaller (1.0 = the size it was modelled at). */
    private static final float SCALE = 2.0F;

    public LeviathanRenderer(EntityRendererProvider.Context context) {
        super(context, new LeviathanModel(context.bakeLayer(LeviathanModel.LAYER)), 2.0F);
        addLayer(new BossGlowLayer<>(this, "leviathan"));
    }

    @Override
    public ResourceLocation getTextureLocation(Leviathan entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Leviathan entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Keep drawing while its laser is on screen, even if the body isn't. */
    @Override
    public boolean shouldRender(Leviathan entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.hasActiveAttackTarget();
    }

    @Override
    public void render(Leviathan entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        LivingEntity target = entity.getActiveAttackTarget();
        if (target == null) return;

        float charge = entity.getAttackAnimationScale(partialTick);
        float time = entity.getClientSideAttackTime() + partialTick;
        float eye = entity.getEyeHeight();
        Vec3 from = entity.getPosition(partialTick).add(0, eye, 0);
        Vec3 to = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.5, 0);
        Vec3 dir = to.subtract(from);
        float length = (float) dir.length();
        if (length < 0.01F) return;
        dir = dir.normalize();

        poseStack.pushPose();
        poseStack.translate(0.0F, eye, 0.0F);
        // Turn so +Y points at the target
        poseStack.mulPose(Axis.YP.rotationDegrees((Mth.HALF_PI - (float) Math.atan2(dir.z, dir.x)) * Mth.RAD_TO_DEG));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.acos(dir.y) * Mth.RAD_TO_DEG));

        // Deep blue while charging, white-hot cyan when it fires
        int r = (int) (20 + 200 * charge * charge);
        int g = (int) (90 + 160 * charge);
        int b = 255;
        float width = 0.12F + 0.38F * charge;
        float v0 = -time * 0.06F;
        float v1 = v0 + length * 0.6F;
        VertexConsumer buffer = buffers.getBuffer(BEAM);
        PoseStack.Pose pose = poseStack.last();
        // Two crossed ribbons, so it looks solid from every side
        for (int i = 0; i < 2; i++) {
            float spin = time * 0.08F + i * Mth.HALF_PI;
            float dx = Mth.cos(spin) * width;
            float dz = Mth.sin(spin) * width;
            vertex(buffer, pose, -dx, 0.0F, -dz, r, g, b, 0.0F, v0);
            vertex(buffer, pose, dx, 0.0F, dz, r, g, b, 1.0F, v0);
            vertex(buffer, pose, dx, length, dz, r, g, b, 1.0F, v1);
            vertex(buffer, pose, -dx, length, -dz, r, g, b, 0.0F, v1);
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z,
                               int r, int g, int b, float u, float v) {
        buffer.addVertex(pose, x, y, z)
            .setColor(r, g, b, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
