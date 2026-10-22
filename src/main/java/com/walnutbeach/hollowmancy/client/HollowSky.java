package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Hollow's own sky: a deep violet dome, slowly turning stars that twinkle,
 * aurora ribbons that ripple across the sky, a pale moon and a faint second one.
 * Lighting stays End-like (always the same brightness, no day and night).
 *
 * Tweak the numbers at the top to taste.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HollowSky extends DimensionSpecialEffects {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "the_hollow");
    private static final ResourceLocation MOON = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/environment/hollow_moon.png");
    private static final ResourceLocation MOON_SMALL = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/environment/hollow_moon_small.png");

    // ---- Tuning ----
    private static final int STAR_COUNT = 1800;
    private static final float STAR_SPIN = 0.0025F;        // degrees per tick the starfield turns
    private static final float AURORA_STRENGTH = 0.32F;    // 0 = off, 1 = very bright
    private static final float MOON_SIZE = 16.0F;
    private static final float MOON_ALPHA = 0.85F;
    private static final float SECOND_MOON_SIZE = 7.0F;
    private static final float SECOND_MOON_ALPHA = 0.38F;  // "faint"
    private static final float ZENITH_R = 0.020F, ZENITH_G = 0.010F, ZENITH_B = 0.050F;

    /** Aurora ribbons: start azimuth, span, base elevation (all degrees), colour bottom (r,g,b), colour top (r,g,b). */
    private static final float[][] AURORAS = {
            {200, 150, 32, 0.30F, 1.00F, 0.78F, 0.55F, 0.35F, 1.00F},
            {20, 120, 40, 0.70F, 0.35F, 1.00F, 0.30F, 0.85F, 1.00F},
            {110, 100, 26, 0.35F, 0.85F, 1.00F, 0.95F, 0.40F, 0.85F},
    };

    private VertexBuffer stars, brightStars;

    @SubscribeEvent
    public static void register(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ID, new HollowSky());
    }

    public HollowSky() {
        super(Float.NaN, false, SkyType.NONE, true, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.scale(0.15F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        if (isFoggy) return true;
        float time = ticks + partialTick;
        float[] fog = RenderSystem.getShaderFogColor();
        float fr = fog[0], fg = fog[1], fb = fog[2];

        PoseStack pose = new PoseStack();
        pose.mulPose(modelViewMatrix);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        FogRenderer.setupNoFog();

        drawDome(pose.last().pose(), fr, fg, fb);

        // Everything that glows adds light on top of the dome
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        // Stars: two layers that twinkle out of step with each other
        if (stars == null) {
            stars = buildStars(10842L, STAR_COUNT, 0.12F, 0.28F);
            brightStars = buildStars(7731L, STAR_COUNT / 12, 0.30F, 0.55F);
        }
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(-22.0F));
        pose.mulPose(Axis.YP.rotationDegrees(time * STAR_SPIN));
        RenderSystem.setShaderColor(1, 1, 1, 0.80F + 0.20F * Mth.sin(time * 0.045F));
        drawBuffer(stars, pose.last().pose(), projectionMatrix);
        RenderSystem.setShaderColor(1, 1, 1, 0.70F + 0.30F * Mth.sin(time * 0.071F + 2.0F));
        drawBuffer(brightStars, pose.last().pose(), projectionMatrix);
        pose.popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        drawAuroras(pose.last().pose(), time);

        // Moons: normal transparency, so their dark side hides the stars behind them
        RenderSystem.defaultBlendFunc();
        drawMoon(pose, MOON, -35.0F + time * 0.0008F, -58.0F, MOON_SIZE, MOON_ALPHA);
        drawMoon(pose, MOON_SMALL, 140.0F - time * 0.0021F, -38.0F, SECOND_MOON_SIZE, SECOND_MOON_ALPHA);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        return true;
    }

    // ---------------- Pieces ----------------

    /** A sphere around the camera: fog colour at the horizon, deep violet overhead, darker below. */
    private static void drawDome(Matrix4f m, float fr, float fg, float fb) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int latStep = 9, lonStep = 12;
        for (int lat = -90; lat < 90; lat += latStep) {
            float[] c0 = domeColour(lat, fr, fg, fb), c1 = domeColour(lat + latStep, fr, fg, fb);
            for (int lon = 0; lon < 360; lon += lonStep) {
                vertex(b, m, lon, lat, 110, c0, 1);
                vertex(b, m, lon + lonStep, lat, 110, c0, 1);
                vertex(b, m, lon + lonStep, lat + latStep, 110, c1, 1);
                vertex(b, m, lon, lat + latStep, 110, c1, 1);
            }
        }
        draw(b);
    }

    private static float[] domeColour(float lat, float fr, float fg, float fb) {
        if (lat <= 0) {
            float t = Mth.clamp(-lat / 50.0F, 0, 1);
            return new float[]{Mth.lerp(t, fr, fr * 0.35F), Mth.lerp(t, fg, fg * 0.35F), Mth.lerp(t, fb, fb * 0.45F)};
        }
        float t = (float) Math.pow(lat / 90.0F, 0.55F);
        // a soft violet glow just above the horizon, fading to the zenith colour
        float gr = fr + 0.035F, gg = fg + 0.012F, gb = fb + 0.060F;
        return new float[]{Mth.lerp(t, gr, ZENITH_R), Mth.lerp(t, gg, ZENITH_G), Mth.lerp(t, gb, ZENITH_B)};
    }

    private static void vertex(BufferBuilder b, Matrix4f m, float lonDeg, float latDeg, float r, float[] c, float a) {
        float lon = lonDeg * Mth.DEG_TO_RAD, lat = latDeg * Mth.DEG_TO_RAD;
        float cl = Mth.cos(lat);
        b.addVertex(m, r * cl * Mth.cos(lon), r * Mth.sin(lat), r * cl * Mth.sin(lon)).setColor(c[0], c[1], c[2], a);
    }

    /** Ribbons of light that ripple sideways and brighten and dim slowly. */
    private static void drawAuroras(Matrix4f m, float time) {
        if (AURORA_STRENGTH <= 0) return;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int segments = 56;
        for (int r = 0; r < AURORAS.length; r++) {
            float[] a = AURORAS[r];
            float pulse = 0.65F + 0.35F * Mth.sin(time * 0.011F + r * 2.1F);
            for (int i = 0; i < segments; i++) {
                float t0 = i / (float) segments, t1 = (i + 1) / (float) segments;
                ribbonQuad(b, m, a, r, t0, t1, time, pulse);
            }
        }
        draw(b);
    }

    private static void ribbonQuad(BufferBuilder b, Matrix4f m, float[] a, int r, float t0, float t1, float time, float pulse) {
        float[] p0 = ribbonPoint(a, r, t0, time), p1 = ribbonPoint(a, r, t1, time);
        float i0 = intensity(t0, r, time) * pulse * AURORA_STRENGTH, i1 = intensity(t1, r, time) * pulse * AURORA_STRENGTH;
        float[] bottom = {a[3], a[4], a[5]}, top = {a[6], a[7], a[8]};
        float height = 16.0F, skirt = 2.5F;
        // main curtain: bright at the bottom, fading out upward
        vertex(b, m, p0[0], p0[1], 100, bottom, i0);
        vertex(b, m, p1[0], p1[1], 100, bottom, i1);
        vertex(b, m, p1[0], p1[1] + height * p1[2], 100, top, 0);
        vertex(b, m, p0[0], p0[1] + height * p0[2], 100, top, 0);
        // a short soft edge below
        vertex(b, m, p0[0], p0[1] - skirt, 100, bottom, 0);
        vertex(b, m, p1[0], p1[1] - skirt, 100, bottom, 0);
        vertex(b, m, p1[0], p1[1], 100, bottom, i1);
        vertex(b, m, p0[0], p0[1], 100, bottom, i0);
    }

    /** Longitude, elevation and height scale of a ribbon at position t (0..1) along it. */
    private static float[] ribbonPoint(float[] a, int r, float t, float time) {
        float lon = a[0] + a[1] * t + 4.0F * Mth.sin(t * 9.0F + time * 0.013F + r);
        float lat = a[2] + 7.0F * Mth.sin(t * 4.0F + r * 1.7F) + 2.5F * Mth.sin(t * 13.0F - time * 0.02F);
        float h = 0.75F + 0.35F * Mth.sin(t * 7.0F + time * 0.017F + r * 0.9F);
        return new float[]{lon, lat, h};
    }

    private static float intensity(float t, int r, float time) {
        float edge = Mth.sin(t * Mth.PI);                        // fades in and out at the ends
        float flicker = 0.75F + 0.25F * Mth.sin(t * 23.0F + time * 0.06F + r * 3.0F);
        return edge * edge * flicker;
    }

    private static void drawMoon(PoseStack pose, ResourceLocation texture, float yaw, float pitch, float size, float alpha) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        Matrix4f m = pose.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.addVertex(m, -size, 100, -size).setUv(0, 0);
        b.addVertex(m, size, 100, -size).setUv(1, 0);
        b.addVertex(m, size, 100, size).setUv(1, 1);
        b.addVertex(m, -size, 100, size).setUv(0, 1);
        draw(b);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        pose.popPose();
    }

    private static VertexBuffer buildStars(long seed, int count, float minSize, float maxSize) {
        RandomSource random = RandomSource.create(seed);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int made = 0;
        while (made < count) {
            float x = random.nextFloat() * 2 - 1, y = random.nextFloat() * 2 - 1, z = random.nextFloat() * 2 - 1;
            float d2 = x * x + y * y + z * z;
            if (d2 < 0.01F || d2 > 1) continue;
            made++;
            Vector3f dir = new Vector3f(x, y, z).normalize(100.0F);
            float size = minSize + random.nextFloat() * (maxSize - minSize);
            // white, pale cyan or lavender
            float tint = random.nextFloat();
            float cr = tint < 0.6F ? 1.0F : tint < 0.8F ? 0.70F : 0.90F;
            float cg = tint < 0.6F ? 1.0F : tint < 0.8F ? 0.95F : 0.72F;
            float cb = 1.0F;
            float alpha = 0.45F + random.nextFloat() * 0.55F;
            Quaternionf q = new Quaternionf().rotateTo(new Vector3f(0, 0, -1), dir).rotateZ(random.nextFloat() * Mth.TWO_PI);
            for (float[] c : new float[][]{{size, -size}, {size, size}, {-size, size}, {-size, -size}}) {
                Vector3f v = new Vector3f(c[0], c[1], 0).rotate(q).add(dir);
                b.addVertex(v.x, v.y, v.z).setColor(cr, cg, cb, alpha);
            }
        }
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(b.buildOrThrow());
        VertexBuffer.unbind();
        return buffer;
    }

    private static void drawBuffer(VertexBuffer buffer, Matrix4f modelView, Matrix4f projection) {
        buffer.bind();
        buffer.drawWithShader(modelView, projection, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
    }

    private static void draw(BufferBuilder b) {
        MeshData mesh = b.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
    }
}
