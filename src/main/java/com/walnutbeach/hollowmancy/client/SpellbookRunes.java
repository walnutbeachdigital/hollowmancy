package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

/** While the spellbook screen is open, a few runes rise out of the open pages every second or two. */
@EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
public class SpellbookRunes {
    /** Ticks between bursts (20 ticks = 1 second): a random wait between these two. */
    private static final int MIN_WAIT = 25, MAX_WAIT = 45;
    /** Runes per burst: a random count between these two. */
    private static final int MIN_RUNES = 2, MAX_RUNES = 4;
    /**
     * Where the open pages are on your screen: 0,0 is the middle, 1 is the right edge, -1 the bottom edge.
     * Nudge these if the runes don't line up with the pages.
     */
    private static final double PAGES_X = 0.80, PAGES_Y = -0.12;
    /** How far in front of the camera they appear (blocks). */
    private static final double DEPTH = 1.2;

    private static int wait = 10;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !(mc.screen instanceof SpellbookScreen) || !mc.options.getCameraType().isFirstPerson()) {
            wait = 12;   // the first burst comes just after the book has opened
            return;
        }
        boolean holding = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().equals("spellbook")
            || BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem()).getPath().equals("spellbook");
        if (!holding || --wait > 0) return;

        RandomSource random = player.getRandom();
        wait = MIN_WAIT + random.nextInt(MAX_WAIT - MIN_WAIT + 1);

        // Turn the spot on screen into a spot in the world, using your field of view and window shape
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f f = camera.getLookVector(), u = camera.getUpVector(), l = camera.getLeftVector();
        Vec3 forward = new Vec3(f.x(), f.y(), f.z()), up = new Vec3(u.x(), u.y(), u.z()), right = new Vec3(-l.x(), -l.y(), -l.z());
        double halfH = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0) * DEPTH;
        double halfW = halfH * mc.getWindow().getWidth() / (double) mc.getWindow().getHeight();

        int count = MIN_RUNES + random.nextInt(MAX_RUNES - MIN_RUNES + 1);
        for (int i = 0; i < count; i++) {
            double sx = PAGES_X + (random.nextDouble() - 0.5) * 0.14;
            double sy = PAGES_Y + (random.nextDouble() - 0.5) * 0.08;
            Vec3 page = camera.getPosition().add(forward.scale(DEPTH)).add(right.scale(sx * halfW)).add(up.scale(sy * halfH));
            // Enchanting runes fly from (x + dx) to x: start on the page, float up and out
            Vec3 to = page.add(up.scale(0.25 + random.nextDouble() * 0.3)).add(right.scale((random.nextDouble() - 0.5) * 0.15));
            Vec3 d = page.subtract(to);
            mc.level.addParticle(ParticleTypes.ENCHANT, to.x, to.y, to.z, d.x, d.y, d.z);
        }
    }
}
