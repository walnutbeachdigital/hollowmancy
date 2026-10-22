package com.walnutbeach.hollowmancy.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import com.walnutbeach.hollowmancy.fx.BeamPayload;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws spell beams: a soft coloured glow, a brighter band and a white-hot core, all additive.
 * Lightning beams are broken into jagged segments that re-roll every 2 ticks, so they flicker.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
public class BeamRenderer {
    private static final int MAX_BEAMS = 64;
    private static final List<Beam> BEAMS = new ArrayList<>();

    private static final class Beam {
        final Vec3 from, to;
        final float r, g, b, width;
        final int life, style;
        final long seed;
        int age;   // negative while a delayed flow pulse is still waiting

        Beam(BeamPayload p, long seed) {
            this.from = p.from();
            this.to = p.to();
            this.r = ((p.color() >> 16) & 0xFF) / 255.0F;
            this.g = ((p.color() >> 8) & 0xFF) / 255.0F;
            this.b = (p.color() & 0xFF) / 255.0F;
            this.width = p.width();
            this.life = Math.max(2, p.life());
            this.style = p.style() & 0xFF;
            this.age = -(p.style() >>> 8);
            this.seed = seed;
        }
    }

    public static void add(BeamPayload payload) {
        if (BEAMS.size() >= MAX_BEAMS) BEAMS.remove(0);
        BEAMS.add(new Beam(payload, RandomSource.create().nextLong()));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (BEAMS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { BEAMS.clear(); return; }
        if (mc.isPaused()) return;
        BEAMS.removeIf(beam -> ++beam.age >= beam.life);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BEAMS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Matrix4f matrix = event.getPoseStack().last().pose();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.lightning());
        for (Beam beam : BEAMS) draw(buffer, matrix, beam, cam, partialTick);
        buffers.endBatch(RenderType.lightning());
    }

    private static void draw(VertexConsumer buffer, Matrix4f m, Beam beam, Vec3 cam, float partialTick) {
        float age = beam.age + partialTick;
        if (age < 0) return;
        if (beam.style == SpellFx.FLOW) {
            drawPulse(buffer, m, beam, cam, age);
            return;
        }
        float fadeIn = Mth.clamp(age / 1.5F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((beam.life - age) / Math.min(6.0F, beam.life * 0.6F), 0.0F, 1.0F);
        float strength = fadeIn * fadeOut;
        if (strength <= 0.01F) return;
        boolean calm = HollowmancyExtraConfig.get(HollowmancyExtraConfig.REDUCED_FLASHING);
        float pulse = calm ? 1.0F : 1.0F + 0.12F * Mth.sin(age * 1.7F);
        float w = beam.width * pulse * (0.6F + 0.4F * fadeOut);

        List<Vec3> points = new ArrayList<>();
        Vec3 from = beam.from.subtract(cam), to = beam.to.subtract(cam);
        if (beam.style == SpellFx.LIGHTNING) {
            Vec3 dir = to.subtract(from);
            double length = dir.length();
            int segments = Math.max(3, (int) (length * 1.6));
            RandomSource random = RandomSource.create(calm ? beam.seed : beam.seed + beam.age / 2);
            Vec3 side1 = perpendicular(dir), side2 = dir.normalize().cross(side1);
            points.add(from);
            for (int i = 1; i < segments; i++) {
                double t = (double) i / segments;
                double amp = 0.35 * Math.sin(Math.PI * t) * Math.min(1.0, length / 4.0);
                points.add(from.add(dir.scale(t))
                        .add(side1.scale((random.nextDouble() - 0.5) * 2 * amp))
                        .add(side2.scale((random.nextDouble() - 0.5) * 2 * amp)));
            }
            points.add(to);
        } else {
            points.add(from);
            points.add(to);
        }

        for (int i = 0; i + 1 < points.size(); i++) {
            Vec3 a = points.get(i), b = points.get(i + 1);
            ribbon(buffer, m, a, b, w * 2.6F, beam.r, beam.g, beam.b, 0.22F * strength);   // soft glow
            ribbon(buffer, m, a, b, w * 1.2F, beam.r, beam.g, beam.b, 0.55F * strength);   // body
            ribbon(buffer, m, a, b, w * 0.45F, Mth.lerp(0.7F, beam.r, 1.0F), Mth.lerp(0.7F, beam.g, 1.0F),
                    Mth.lerp(0.7F, beam.b, 1.0F), 0.9F * strength);                            // core
        }
    }

    /** A bright blob gliding from `from` to `to`, with a short fading tail behind it. */
    private static void drawPulse(VertexConsumer buffer, Matrix4f m, Beam beam, Vec3 cam, float age) {
        float t = Mth.clamp(age / beam.life, 0.0F, 1.0F);
        Vec3 from = beam.from.subtract(cam), to = beam.to.subtract(cam);
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 1.0E-4) return;
        Vec3 unit = dir.scale(1.0 / length);
        Vec3 head = from.add(dir.scale(t));
        double tail = Math.min(0.9, length * t);
        Vec3 tailStart = head.subtract(unit.scale(tail));
        float w = beam.width;
        ribbon(buffer, m, tailStart, head, w * 1.6F, beam.r, beam.g, beam.b, 0.25F);
        Vec3 h0 = head.subtract(unit.scale(0.14)), h1 = head.add(unit.scale(0.14));
        ribbon(buffer, m, h0, h1, w * 4.0F, beam.r, beam.g, beam.b, 0.35F);
        ribbon(buffer, m, h0, h1, w * 2.2F, Mth.lerp(0.6F, beam.r, 1.0F), Mth.lerp(0.6F, beam.g, 1.0F), Mth.lerp(0.6F, beam.b, 1.0F), 0.85F);
    }

    /** A flat strip from a to b, turned to face the camera (which sits at 0,0,0 here). */
    private static void ribbon(VertexConsumer buffer, Matrix4f m, Vec3 a, Vec3 b, float width, float r, float g, float bl, float alpha) {
        Vec3 dir = b.subtract(a);
        Vec3 mid = a.add(b).scale(0.5);
        Vec3 side = dir.cross(mid);
        if (side.lengthSqr() < 1.0E-8) side = perpendicular(dir);
        side = side.normalize().scale(width / 2.0);
        Vec3 p1 = a.subtract(side), p2 = a.add(side), p3 = b.add(side), p4 = b.subtract(side);
        quad(buffer, m, p1, p2, p3, p4, r, g, bl, alpha);
        quad(buffer, m, p4, p3, p2, p1, r, g, bl, alpha);
    }

    private static void quad(VertexConsumer buffer, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, float r, float g, float b, float a) {
        buffer.addVertex(m, (float) p1.x, (float) p1.y, (float) p1.z).setColor(r, g, b, a);
        buffer.addVertex(m, (float) p2.x, (float) p2.y, (float) p2.z).setColor(r, g, b, a);
        buffer.addVertex(m, (float) p3.x, (float) p3.y, (float) p3.z).setColor(r, g, b, a);
        buffer.addVertex(m, (float) p4.x, (float) p4.y, (float) p4.z).setColor(r, g, b, a);
    }

    private static Vec3 perpendicular(Vec3 dir) {
        Vec3 up = Math.abs(dir.normalize().y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        return dir.cross(up).normalize();
    }
}
