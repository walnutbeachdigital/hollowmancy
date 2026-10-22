package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * Minecraft hides your arm when you hold an item. While you hold the spellbook,
 * this draws your arm underneath it (the same pose the game uses for holding a map).
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
public class SpellbookHand {
    /**
     * Where your hand sits, so it holds the bottom of the book: right (+) / left (-), up (+) / down (-), in blocks.
     * CLOSED = while you carry it shut, OPEN = while the spellbook screen is open. The arm glides between them.
     */
    private static final float CLOSED_X = -0.30F, CLOSED_Y = -0.02F;
    private static final float OPEN_X = 0.12F, OPEN_Y = 0.00F;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).getPath().equals("spellbook")) return;
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null || player.isInvisible()) return;

        HumanoidArm side = event.getHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        float f = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float open = SpellbookOpening.openness();
        float armX = Mth.lerp(open, CLOSED_X, OPEN_X), armY = Mth.lerp(open, CLOSED_Y, OPEN_Y);
        pose.translate(f * (0.125F + armX), -0.125F + armY, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
        renderArm(mc, player, pose, event, side, f);
        pose.popPose();
    }

    /** Vanilla's empty-hand arm, as drawn by ItemInHandRenderer. */
    private static void renderArm(Minecraft mc, AbstractClientPlayer player, PoseStack pose, RenderHandEvent event, HumanoidArm side, float f) {
        float swing = event.getSwingProgress();
        float equip = event.getEquipProgress();
        float root = Mth.sqrt(swing);
        pose.translate(f * (-0.3F * Mth.sin(root * Mth.PI) + 0.64F),
                0.4F * Mth.sin(root * Mth.TWO_PI) - 0.6F - equip * 0.6F,
                -0.4F * Mth.sin(swing * Mth.PI) - 0.72F);
        pose.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        pose.mulPose(Axis.YP.rotationDegrees(f * Mth.sin(root * Mth.PI) * 70.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(f * Mth.sin(swing * swing * Mth.PI) * -20.0F));
        pose.translate(f * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        pose.translate(f * 5.6F, 0.0F, 0.0F);
        PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        if (side == HumanoidArm.RIGHT) renderer.renderRightHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
        else renderer.renderLeftHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
    }
}
