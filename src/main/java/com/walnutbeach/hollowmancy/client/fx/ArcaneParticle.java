package com.walnutbeach.hollowmancy.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** One particle class for all of Hollowmancy's particles; a Look says how each kind behaves. */
public class ArcaneParticle extends TextureSheetParticle {
    /**
     * life:      ticks it lasts (random between min and max)
     * size:      starting size
     * gravity:   positive falls, negative rises
     * friction:  1.0 keeps its speed, lower slows it down
     * animate:   play the sprites in order over its life (otherwise pick one at random)
     * glow:      always fully bright
     * spin:      how fast it turns
     * sway:      side-to-side drifting (leaves)
     * jitter:    random twitching (sparks)
     * grow:      how much bigger (+) or smaller (-) it gets by the end
     * color:     tint, 0xFFFFFF for none
     */
    public record Look(int lifeMin, int lifeMax, float size, float gravity, float friction, boolean animate, boolean glow,
                       float spin, float sway, float jitter, float grow, int color) {}

    protected final SpriteSet sprites;
    protected final Look look;
    private final float baseSize, spinSpeed, swayPhase;

    protected ArcaneParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, Look look) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.sprites = sprites;
        this.look = look;
        this.lifetime = look.lifeMin() + random.nextInt(Math.max(1, look.lifeMax() - look.lifeMin() + 1));
        this.gravity = look.gravity();
        this.friction = look.friction();
        this.hasPhysics = false;
        this.quadSize = look.size() * (0.8F + random.nextFloat() * 0.4F);
        this.baseSize = quadSize;
        int c = look.color();
        setColor(((c >> 16) & 0xFF) / 255.0F, ((c >> 8) & 0xFF) / 255.0F, (c & 0xFF) / 255.0F);
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;
        this.spinSpeed = look.spin() * (random.nextBoolean() ? 1 : -1) * (0.5F + random.nextFloat());
        this.swayPhase = random.nextFloat() * Mth.TWO_PI;
        this.alpha = 0.0F;
        if (look.animate()) setSpriteFromAge(sprites);
        else pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        if (look.animate()) setSpriteFromAge(sprites);
        oRoll = roll;
        roll += spinSpeed;
        if (look.sway() > 0) {
            xd += Mth.sin(age * 0.25F + swayPhase) * look.sway() * 0.01F;
            zd += Mth.cos(age * 0.21F + swayPhase) * look.sway() * 0.01F;
        }
        if (look.jitter() > 0) {
            xd += (random.nextFloat() - 0.5F) * look.jitter();
            yd += (random.nextFloat() - 0.5F) * look.jitter();
            zd += (random.nextFloat() - 0.5F) * look.jitter();
        }
        float t = (float) age / lifetime;
        quadSize = Math.max(0.01F, baseSize * (1.0F + look.grow() * t));
        // fade in quickly, hold, fade out
        alpha = t < 0.12F ? t / 0.12F : 1.0F - Math.max(0.0F, (t - 0.55F) / 0.45F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return look.glow() ? 0xF000F0 : super.getLightColor(partialTick);
    }

    /** A spell circle lying flat, spreading out to the radius it was sent with. */
    public static class Ring extends ArcaneParticle {
        private final float radius;

        protected Ring(ClientLevel level, double x, double y, double z, double radius, SpriteSet sprites, Look look) {
            super(level, x, y, z, 0, 0, 0, sprites, look);
            this.radius = (float) Math.max(0.5, radius);
            this.gravity = 0;
        }

        @Override
        public void tick() {
            xo = x; yo = y; zo = z;
            if (age++ >= lifetime) remove();
        }

        @Override
        public void render(VertexConsumer buffer, Camera camera, float partialTick) {
            Vec3 cam = camera.getPosition();
            float px = (float) (Mth.lerp(partialTick, xo, x) - cam.x());
            float py = (float) (Mth.lerp(partialTick, yo, y) - cam.y());
            float pz = (float) (Mth.lerp(partialTick, zo, z) - cam.z());
            float t = Mth.clamp((age + partialTick) / lifetime, 0.0F, 1.0F);
            float r = Mth.lerp(1.0F - (1.0F - t) * (1.0F - t), 0.3F, radius);
            float a = t < 0.1F ? t / 0.1F : 1.0F - (t - 0.1F) / 0.9F;
            float u0 = getU0(), u1 = getU1(), v0 = getV0(), v1 = getV1();
            int light = 0xF000F0;
            // both sides, so it shows from above and below
            vertex(buffer, px - r, py, pz - r, u0, v0, a, light);
            vertex(buffer, px - r, py, pz + r, u0, v1, a, light);
            vertex(buffer, px + r, py, pz + r, u1, v1, a, light);
            vertex(buffer, px + r, py, pz - r, u1, v0, a, light);
            vertex(buffer, px + r, py, pz - r, u1, v0, a, light);
            vertex(buffer, px + r, py, pz + r, u1, v1, a, light);
            vertex(buffer, px - r, py, pz + r, u0, v1, a, light);
            vertex(buffer, px - r, py, pz - r, u0, v0, a, light);
        }

        private void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v, float a, int light) {
            buffer.addVertex(x, y, z).setUv(u, v).setColor(rCol, gCol, bCol, a).setLight(light);
        }
    }
}
