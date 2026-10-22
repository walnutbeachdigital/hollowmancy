package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.InfuserBlockEntity;
import com.walnutbeach.hollowmancy.block.ModBlockEntities;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;

/**
 * A small floating display over the Infuser: the offering turning slowly, a ring of swirling mana
 * that fills as the infusion progresses, and a line saying what it's doing.
 */
public class InfuserRenderer implements BlockEntityRenderer<InfuserBlockEntity> {
    /** How close you need to be to see the display (blocks). */
    private static final double RANGE = 6.0;
    private static final int SEGMENTS = 48;
    private static final float RING_IN = 0.30F, RING_OUT = 0.36F;

    private final ItemRenderer items;
    private final Font font;

    public InfuserRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(InfuserBlockEntity infuser, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack input = infuser.getInput().getStackInSlot(0);
        if (input.isEmpty() || mc.player == null || infuser.getLevel() == null) return;
        if (mc.player.distanceToSqr(infuser.getBlockPos().getCenter()) > RANGE * RANGE) return;

        float time = infuser.getLevel().getGameTime() + partialTick;
        float target = infuser.getProgressPercent() / 100.0F;
        if (infuser.shownProgress < 0 || target < infuser.shownProgress - 0.5F) infuser.shownProgress = target;
        infuser.shownProgress += (target - infuser.shownProgress) * 0.15F;
        String problem = infuser.problem();

        pose.pushPose();
        pose.translate(0.5, 1.55 + Mth.sin(time * 0.08F) * 0.03, 0.5);

        // The offering, turning
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(time * 2.0F));
        pose.scale(0.42F, 0.42F, 0.42F);
        items.renderStatic(input, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffers, infuser.getLevel(), 0);
        pose.popPose();

        // The ring and the label face you
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ring(pose, buffers, problem == null ? infuser.shownProgress : 0.0F, problem == null);

        Component label = problem == null
                ? Component.translatable("hollowmancy.infuser.display.working", Math.round(infuser.shownProgress * 100))
                : problemText(problem);
        pose.translate(0, -0.47, 0);
        pose.scale(0.014F, -0.014F, 0.014F);
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch(label, -font.width(label) / 2.0F, 0, problem == null ? 0xFFE2D6FF : 0xFFFFD27A, false, matrix, buffers,
                Font.DisplayMode.NORMAL, 0x70140A2A, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static void ring(PoseStack pose, MultiBufferSource buffers, float fill, boolean working) {
        TextureAtlasSprite swirl = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ManaBatteryRenderer.SWIRL);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pose.last();
        int lit = Math.round(SEGMENTS * Mth.clamp(fill, 0.0F, 1.0F));
        for (int i = 0; i < SEGMENTS; i++) {
            // start at the top and go clockwise
            float a0 = Mth.HALF_PI - Mth.TWO_PI * i / SEGMENTS, a1 = Mth.HALF_PI - Mth.TWO_PI * (i + 1) / SEGMENTS;
            boolean on = i < lit;
            int alpha = on ? 235 : working ? 110 : 70;
            int shade = on ? 255 : 90;
            float u0 = swirl.getU((float) i / SEGMENTS), u1 = swirl.getU((float) (i + 1) / SEGMENTS);
            float v0 = swirl.getV(0.2F), v1 = swirl.getV(0.8F);
            vertex(buffer, last, Mth.cos(a0) * RING_IN, Mth.sin(a0) * RING_IN, u0, v1, shade, alpha);
            vertex(buffer, last, Mth.cos(a0) * RING_OUT, Mth.sin(a0) * RING_OUT, u0, v0, shade, alpha);
            vertex(buffer, last, Mth.cos(a1) * RING_OUT, Mth.sin(a1) * RING_OUT, u1, v0, shade, alpha);
            vertex(buffer, last, Mth.cos(a1) * RING_IN, Mth.sin(a1) * RING_IN, u1, v1, shade, alpha);
        }
    }

    private static void vertex(VertexConsumer b, PoseStack.Pose pose, float x, float y, float u, float v, int shade, int alpha) {
        b.addVertex(pose, x, y, 0)
                .setColor(shade, shade, shade, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0, 0, 1);
    }

    /** The Infuser's status, in the player's language. */
    static Component problemText(String problem) {
        java.util.regex.Matcher waiting = java.util.regex.Pattern.compile("waiting for mana \\((\\d+)/(\\d+)\\)\\.").matcher(problem);
        if (waiting.matches()) return Component.translatable("hollowmancy.infuser.problem.waiting", waiting.group(1), waiting.group(2));
        return switch (problem) {
            case "idle. Give it an Infusion offering." -> Component.translatable("hollowmancy.infuser.problem.idle");
            case "this infusion is beyond whoever attuned it." -> Component.translatable("hollowmancy.infuser.problem.beyond");
            case "full. Take the finished item." -> Component.translatable("hollowmancy.infuser.problem.full");
            default -> Component.literal(problem);
        };
    }

    @Override
    public boolean shouldRenderOffScreen(InfuserBlockEntity infuser) {
        return true;
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent
        public static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.INFUSER.get(), InfuserRenderer::new);
        }
    }
}
