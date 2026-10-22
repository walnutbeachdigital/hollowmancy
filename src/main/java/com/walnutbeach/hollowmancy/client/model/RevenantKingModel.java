package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.RevenantKing;
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
import net.minecraft.client.model.ArmedModel;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/revenant_king.bbmodel).
 * Texture: textures/entity/revenant_king.png (128x128), glow: textures/entity/revenant_king_glow.png.
 */
public class RevenantKingModel extends HierarchicalModel<RevenantKing> implements ArmedModel {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "revenant_king"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tabard;
    private final ModelPart cape;
    private final ModelPart head;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public RevenantKingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tabard = this.body.getChild("tabard");
        this.cape = this.body.getChild("cape");
        this.head = this.body.getChild("head");
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
                .texOffs(28, 0).addBox(-6F, -14F, -3.5F, 12F, 14F, 7F, new CubeDeformation(grow))
                .texOffs(56, 43).addBox(-6.5F, -2F, -4F, 13F, 2F, 8F, new CubeDeformation(grow))
                .texOffs(72, 55).addBox(-4F, -16F, -3F, 8F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 8F, 0F));
        p_body.addOrReplaceChild("tabard", CubeListBuilder.create()
                .texOffs(20, 24).addBox(-3.5F, 0F, -0.5F, 7F, 17F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -9F, -3.6F));
        p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.5F, 0F, 0F, 13F, 23F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -14F, 3.6F, 0.1047F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(36, 24).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, -15F, 0F));
        p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(68, 24).addBox(-4.5F, -3F, -4.5F, 9F, 3F, 9F, new CubeDeformation(0.1F + grow)),
            PartPose.offset(0F, -8F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-2.5F, -2F, -2.5F, 5F, 14F, 5F, new CubeDeformation(grow))
                .texOffs(98, 43).addBox(-3F, 8F, -3F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(-7.5F, -12F, 0F));
        p_right_arm.addOrReplaceChild("right_pauldron", CubeListBuilder.create()
                .texOffs(0, 43).addBox(-3.5F, -3F, -3.5F, 7F, 5F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-2.5F, -2F, -2.5F, 5F, 14F, 5F, new CubeDeformation(grow))
                .texOffs(0, 55).addBox(-3F, 8F, -3F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(7.5F, -12F, 0F));
        p_left_arm.addOrReplaceChild("left_pauldron", CubeListBuilder.create()
                .texOffs(28, 43).addBox(-3.5F, -3F, -3.5F, 7F, 5F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, 0.1745F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-2.5F, 0F, -2.5F, 5F, 16F, 5F, new CubeDeformation(grow))
                .texOffs(100, 55).addBox(-2.5F, 5F, -3F, 5F, 4F, 1F, new CubeDeformation(grow))
                .texOffs(24, 55).addBox(-3F, 12F, -3.5F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(-3F, 8F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-2.5F, 0F, -2.5F, 5F, 16F, 5F, new CubeDeformation(grow))
                .texOffs(112, 55).addBox(-2.5F, 5F, -3F, 5F, 4F, 1F, new CubeDeformation(grow))
                .texOffs(48, 55).addBox(-3F, 12F, -3.5F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(3F, 8F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(RevenantKing entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        float walk = limbSwing * 0.6662F;
        right_leg.xRot = Mth.cos(walk) * 1.1F * limbSwingAmount;
        left_leg.xRot = Mth.cos(walk + Mth.PI) * 1.1F * limbSwingAmount;
        right_arm.xRot += Mth.cos(walk + Mth.PI) * 0.7F * limbSwingAmount;
        left_arm.xRot += Mth.cos(walk) * 0.7F * limbSwingAmount;

        // Breathing, and the cape and tabard trailing as he walks
        float breathe = Mth.cos(ageInTicks * 0.09F) * 0.04F;
        right_arm.zRot += breathe;
        left_arm.zRot -= breathe;
        cape.xRot += limbSwingAmount * 0.9F + Mth.sin(ageInTicks * 0.1F) * 0.04F;
        tabard.xRot -= limbSwingAmount * 0.35F;

        // Leaping: blade raised overhead, cape streaming
        if (!entity.onGround()) {
            right_arm.xRot = -2.7F;
            left_arm.xRot = -2.3F;
            right_leg.xRot = -0.7F;
            left_leg.xRot = 0.3F;
            cape.xRot += 0.7F;
        }

        // Cleave: from high over the shoulder down through the target
        if (attackTime > 0.0F) {
            float swing = Mth.sin(Mth.sqrt(attackTime) * Mth.PI);
            right_arm.xRot = Mth.lerp(attackTime, -2.9F, 0.4F);
            right_arm.yRot = -0.3F * swing;
            body.yRot = -0.5F * swing;
            head.yRot += 0.4F * swing;
        }

    }

    /** Where held items go: the end of the arm (2 px longer than a player's). */
    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        body.translateAndRotate(poseStack);
        (side == HumanoidArm.LEFT ? left_arm : right_arm).translateAndRotate(poseStack);
        poseStack.translate(0.0F, 0.125F, 0.0F);
    }
}
