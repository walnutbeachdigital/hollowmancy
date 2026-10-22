package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.castle.RevenantGuard;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Generated from the Hollowmancy boss model generator (Blockbench project: the Revenant Guard design).
 * Texture: textures/entity/revenant_guard.png (64x64), glow: textures/entity/revenant_guard_glow.png.
 */
public class RevenantGuardModel extends HierarchicalModel<RevenantGuard> implements ArmedModel {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "revenant_guard"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tabard;
    private final ModelPart cape;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart plume;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public RevenantGuardModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tabard = this.body.getChild("tabard");
        this.cape = this.body.getChild("cape");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.plume = this.head.getChild("plume");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(0.0F);
    }

    /** grow: puffs every cube out (used for the energy shell). */
    public static LayerDefinition createBodyLayer(float grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1F, -12F, -1F, 2F, 12F, 2F, new CubeDeformation(grow))
                .texOffs(28, 16).addBox(-4F, -12F, -2F, 8F, 7F, 4F, new CubeDeformation(grow))
                .texOffs(0, 48).addBox(-4.5F, -12.5F, -2.6F, 9F, 2F, 5F, new CubeDeformation(grow))
                .texOffs(12, 55).addBox(-3.5F, -2.5F, -1.5F, 7F, 2F, 3F, new CubeDeformation(grow))
                .texOffs(32, 55).addBox(-4F, -4F, -2.2F, 8F, 1F, 4F, new CubeDeformation(grow)),
            PartPose.offset(0F, 12F, 0F));
        p_body.addOrReplaceChild("tabard", CubeListBuilder.create()
                .texOffs(52, 16).addBox(-2.5F, 0F, -0.5F, 5F, 10F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -3F, -2.4F));
        p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 0F, 0F, 7F, 15F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -12F, 2.3F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.5F, -7F, -3.5F, 7F, 6F, 7F, new CubeDeformation(grow))
                .texOffs(0, 29).addBox(-4F, -8.5F, -4F, 8F, 3F, 8F, new CubeDeformation(0.15F + grow))
                .texOffs(56, 55).addBox(-0.5F, -5.5F, -4.4F, 1F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -12.5F, 0F));
        p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(28, 48).addBox(-2.5F, 0F, -3F, 5F, 1F, 5F, new CubeDeformation(grow)),
            PartPose.offset(0F, -1F, -0.5F));
        p_head.addOrReplaceChild("plume", CubeListBuilder.create()
                .texOffs(32, 29).addBox(-0.5F, -3F, -3F, 1F, 3F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -8.5F, 0F, -0.2618F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-1F, -1F, -1F, 2F, 12F, 2F, new CubeDeformation(grow))
                .texOffs(48, 48).addBox(-1.5F, 6F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5F, -11F, 0F, 0F, 0F, -0.0524F));
        p_right_arm.addOrReplaceChild("pauldron", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-3F, -2F, -2.5F, 5F, 3F, 5F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, -0.2443F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1F, -1F, -1F, 2F, 12F, 2F, new CubeDeformation(grow))
                .texOffs(0, 55).addBox(-1.5F, 6F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5F, -11F, 0F, 0F, 0F, 0.0524F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F, new CubeDeformation(grow))
                .texOffs(20, 40).addBox(-1.5F, 6F, -1.6F, 3F, 5F, 3F, new CubeDeformation(grow)),
            PartPose.offset(-2F, 12F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F, new CubeDeformation(grow))
                .texOffs(32, 40).addBox(-1.5F, 6F, -1.6F, 3F, 5F, 3F, new CubeDeformation(grow)),
            PartPose.offset(2F, 12F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(RevenantGuard entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        float walk = limbSwing * 0.6662F;
        right_leg.xRot = Mth.cos(walk) * 1.1F * limbSwingAmount;
        left_leg.xRot = Mth.cos(walk + Mth.PI) * 1.1F * limbSwingAmount;
        right_arm.xRot += Mth.cos(walk + Mth.PI) * 0.8F * limbSwingAmount;
        left_arm.xRot += Mth.cos(walk) * 0.8F * limbSwingAmount;

        // The jaw hangs a little loose; the cape and tabard trail as it walks
        jaw.xRot = 0.08F + Mth.sin(ageInTicks * 0.08F) * 0.06F;
        cape.xRot += limbSwingAmount * 0.8F + Mth.sin(ageInTicks * 0.09F) * 0.04F;
        tabard.xRot -= limbSwingAmount * 0.4F;
        plume.xRot += limbSwingAmount * 0.3F;
        if (entity.isAggressive()) {
            if (entity.getMainHandItem().is(Items.BOW)) {
                // Drawing the bow, like a skeleton
                right_arm.xRot = -Mth.HALF_PI + head.xRot;
                right_arm.yRot = -0.1F + head.yRot;
                left_arm.xRot = -Mth.HALF_PI + head.xRot;
                left_arm.yRot = 0.5F + head.yRot;
            } else {
                right_arm.xRot = -1.1F;
                jaw.xRot = 0.35F;
            }
        }
        // Sword cut
        if (attackTime > 0.0F) {
            float swing = Mth.sin(Mth.sqrt(attackTime) * Mth.PI);
            right_arm.xRot = Mth.lerp(attackTime, -2.5F, 0.3F);
            body.yRot = -0.3F * swing;
        }

    }

    /** Where held items go: the end of the arm (0 px longer than a player's). */
    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        body.translateAndRotate(poseStack);
        // Thin bone arms: nudge outward by a pixel, like a skeleton, so held items sit in the hand
        ModelPart arm = side == HumanoidArm.LEFT ? left_arm : right_arm;
        float nudge = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        arm.x += nudge;
        arm.translateAndRotate(poseStack);
        arm.x -= nudge;
    }
}
