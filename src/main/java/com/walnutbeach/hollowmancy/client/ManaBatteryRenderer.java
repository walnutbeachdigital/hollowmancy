package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.ManaBatteryBlockEntity;
import com.walnutbeach.hollowmancy.block.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;

/**
 * Draws the mana inside a battery's glass at its exact level (easing smoothly when it changes), in the
 * animated mana texture, glowing in the dark. Looking at the battery shows the exact amount above it.
 */
public class ManaBatteryRenderer implements BlockEntityRenderer<ManaBatteryBlockEntity> {

    public static final ResourceLocation SWIRL = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "block/mana_swirl");

    // The space inside the glass, in 1/16ths of a block
    private static final float X0 = 4.5f, X1 = 11.5f, Y0 = 3.2f, Y_FULL = 13.0f;
    /** How close you need to be to see the number (blocks). */
    private static final double LABEL_RANGE = 8.0;

    private final Font font;

    public ManaBatteryRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(ManaBatteryBlockEntity battery, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        // Ease the shown level toward the real amount, so filling and draining look smooth
        float target = battery.getMana();
        if (battery.shownMana < 0) battery.shownMana = target;
        battery.shownMana += (target - battery.shownMana) * 0.12f;
        if (Math.abs(target - battery.shownMana) < 0.5f) battery.shownMana = target;
        float fill = Mth.clamp(battery.shownMana / ManaBatteryBlockEntity.CAPACITY, 0.0f, 1.0f);

        if (fill > 0.001f) {
            TextureAtlasSprite swirl = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SWIRL);
            VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));   // both sides drawn
            float top = Y0 + (Y_FULL - Y0) * fill;
            column(buffer, poseStack.last(), swirl, X0 / 16f, Y0 / 16f, X1 / 16f, top / 16f);
        }

        drawLabel(battery, poseStack, buffers);
    }

    private static void column(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s, float x0, float y0, float x1, float y1) {
        float h = (y1 - y0) / (x1 - x0);          // keep the texture's pixels square on the sides
        float vTop = Math.min(1.0f, h * (x1 - x0));
        // top
        quad(b, pose, s, x0, y1, x0, x0, y1, x1, x1, y1, x1, x1, y1, x0, 0, 1, 0, 0, 0, 1, 1);
        // four sides
        quad(b, pose, s, x0, y0, x0, x0, y1, x0, x1, y1, x0, x1, y0, x0, 0, 0, -1, 0, 1 - vTop, 1, 1);
        quad(b, pose, s, x1, y0, x1, x1, y1, x1, x0, y1, x1, x0, y0, x1, 0, 0, 1, 0, 1 - vTop, 1, 1);
        quad(b, pose, s, x0, y0, x1, x0, y1, x1, x0, y1, x0, x0, y0, x0, -1, 0, 0, 0, 1 - vTop, 1, 1);
        quad(b, pose, s, x1, y0, x0, x1, y1, x0, x1, y1, x1, x1, y0, x1, 1, 0, 0, 0, 1 - vTop, 1, 1);
    }

    private static void quad(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, float u0, float v0, float u1, float v1) {
        vertex(b, pose, s, ax, ay, az, u0, v1, nx, ny, nz);
        vertex(b, pose, s, bx, by, bz, u0, v0, nx, ny, nz);
        vertex(b, pose, s, cx, cy, cz, u1, v0, nx, ny, nz);
        vertex(b, pose, s, dx, dy, dz, u1, v1, nx, ny, nz);
    }

    private static void vertex(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz) {
        b.addVertex(pose, x, y, z)
            .setColor(255, 255, 255, 235)
            .setUv(s.getU(u), s.getV(v))
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, nx, ny, nz);
    }

    /** "1,247 / 2,000" floating above the battery while you look at it. */
    private void drawLabel(ManaBatteryBlockEntity battery, PoseStack poseStack, MultiBufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (mc.player == null || !(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(battery.getBlockPos())) return;
        if (mc.player.distanceToSqr(battery.getBlockPos().getCenter()) > LABEL_RANGE * LABEL_RANGE) return;

        String text = String.format("%,d / %,d", battery.getMana(), ManaBatteryBlockEntity.CAPACITY);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.3, 0.5);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix = poseStack.last().pose();
        float x = -font.width(text) / 2.0f;
        font.drawInBatch(text, x, 0, 0xFFE2D6FF, false, matrix, buffers, Font.DisplayMode.NORMAL, 0x70140A2A, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent
        public static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.MANA_BATTERY.get(), ManaBatteryRenderer::new);
        }
    }
}
