package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.Castellan;
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
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/castellan.bbmodel).
 * Texture: textures/entity/castellan.png (128x128), glow: textures/entity/castellan_glow.png.
 */
public class CastellanModel extends HierarchicalModel<Castellan> implements ArmedModel {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "castellan"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public CastellanModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
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
                .texOffs(0, 0).addBox(-6F, -12F, -3.5F, 12F, 12F, 7F, new CubeDeformation(grow))
                .texOffs(28, 19).addBox(-5.5F, -11F, -4F, 11F, 12F, 1F, new CubeDeformation(grow))
                .texOffs(0, 33).addBox(-6.5F, -3F, -4.2F, 13F, 2F, 8F, new CubeDeformation(grow))
                .texOffs(48, 43).addBox(5.5F, -2F, -2F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 13F, 0F));
        p_body.addOrReplaceChild("chain", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-0.5F, 0F, -0.5F, 1F, 17F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5F, -11F, -4.4F, 0F, 0F, -0.6632F));
        p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-3.5F, -7F, -3.5F, 7F, 7F, 7F, new CubeDeformation(grow))
                .texOffs(42, 33).addBox(-0.5F, -8F, -4F, 1F, 2F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, -12F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-2.5F, -2F, -2.5F, 5F, 13F, 5F, new CubeDeformation(grow))
                .texOffs(60, 33).addBox(-3F, 7F, -3F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(-8F, -10F, 0F));
        p_right_arm.addOrReplaceChild("right_pauldron", CubeListBuilder.create()
                .texOffs(52, 19).addBox(-3.5F, -3F, -3.5F, 7F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-2.5F, -2F, -2.5F, 5F, 13F, 5F, new CubeDeformation(grow))
                .texOffs(84, 33).addBox(-3F, 7F, -3F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(8F, -10F, 0F));
        p_left_arm.addOrReplaceChild("left_pauldron", CubeListBuilder.create()
                .texOffs(80, 19).addBox(-3.5F, -3F, -3.5F, 7F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, 0.2094F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(0, 43).addBox(-3F, 7F, -3.5F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(-3F, 13F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(102, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(24, 43).addBox(-3F, 7F, -3.5F, 6F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(3F, 13F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Castellan entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        float walk = limbSwing * 0.6662F;
        right_leg.xRot = Mth.cos(walk) * 1.0F * limbSwingAmount;
        left_leg.xRot = Mth.cos(walk + Mth.PI) * 1.0F * limbSwingAmount;
        right_arm.xRot += Mth.cos(walk + Mth.PI) * 0.6F * limbSwingAmount;
        left_arm.xRot += Mth.cos(walk) * 0.6F * limbSwingAmount;

        // The shield is always up
        left_arm.xRot = -0.75F + Mth.cos(limbSwing * 0.6662F) * 0.15F * limbSwingAmount;
        left_arm.yRot = 0.45F;
        float breathe = Mth.cos(ageInTicks * 0.1F) * 0.035F;
        right_arm.zRot += breathe;

        // Axe chop
        if (attackTime > 0.0F) {
            float swing = Mth.sin(Mth.sqrt(attackTime) * Mth.PI);
            right_arm.xRot = Mth.lerp(attackTime, -2.6F, 0.3F);
            body.yRot = -0.35F * swing;
            left_arm.xRot -= 0.3F * swing;
        }

    }

    /** Where held items go: the end of the arm (1 px longer than a player's). */
    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        body.translateAndRotate(poseStack);
        (side == HumanoidArm.LEFT ? left_arm : right_arm).translateAndRotate(poseStack);
        poseStack.translate(0.0F, 0.0625F, 0.0F);
    }
}
