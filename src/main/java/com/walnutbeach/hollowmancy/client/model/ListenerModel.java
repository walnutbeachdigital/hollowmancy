package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.Listener;
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

/**
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/listener.bbmodel).
 * Texture: textures/entity/listener.png (128x128), glow: textures/entity/listener_glow.png.
 */
public class ListenerModel extends HierarchicalModel<Listener> {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "listener"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart heart;
    private final ModelPart mantle;
    private final ModelPart head;
    private final ModelPart head_earr;
    private final ModelPart head_earl;
    private final ModelPart right_head;
    private final ModelPart left_head;
    private final ModelPart right_arm;
    private final ModelPart right_forearm;
    private final ModelPart right_finger0;
    private final ModelPart right_finger1;
    private final ModelPart right_finger2;
    private final ModelPart left_arm;
    private final ModelPart left_forearm;
    private final ModelPart left_finger0;
    private final ModelPart left_finger1;
    private final ModelPart left_finger2;
    private final ModelPart tendril0;
    private final ModelPart tendril0_tip;
    private final ModelPart tendril1;
    private final ModelPart tendril1_tip;
    private final ModelPart tendril2;
    private final ModelPart tendril2_tip;

    public ListenerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.heart = this.body.getChild("heart");
        this.mantle = this.body.getChild("mantle");
        this.head = this.body.getChild("head");
        this.head_earr = this.head.getChild("head_earr");
        this.head_earl = this.head.getChild("head_earl");
        this.right_head = this.body.getChild("right_head");
        this.left_head = this.body.getChild("left_head");
        this.right_arm = this.body.getChild("right_arm");
        this.right_forearm = this.right_arm.getChild("right_forearm");
        this.right_finger0 = this.right_forearm.getChild("right_hand").getChild("right_finger0");
        this.right_finger1 = this.right_forearm.getChild("right_hand").getChild("right_finger1");
        this.right_finger2 = this.right_forearm.getChild("right_hand").getChild("right_finger2");
        this.left_arm = this.body.getChild("left_arm");
        this.left_forearm = this.left_arm.getChild("left_forearm");
        this.left_finger0 = this.left_forearm.getChild("left_hand").getChild("left_finger0");
        this.left_finger1 = this.left_forearm.getChild("left_hand").getChild("left_finger1");
        this.left_finger2 = this.left_forearm.getChild("left_hand").getChild("left_finger2");
        this.tendril0 = this.body.getChild("tendril0");
        this.tendril0_tip = this.tendril0.getChild("tendril0_tip");
        this.tendril1 = this.body.getChild("tendril1");
        this.tendril1_tip = this.tendril1.getChild("tendril1_tip");
        this.tendril2 = this.body.getChild("tendril2");
        this.tendril2_tip = this.tendril2.getChild("tendril2_tip");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(0.0F);
    }

    /** grow: puffs every cube out (used for the energy shell). */
    public static LayerDefinition createBodyLayer(float grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -15F, -1.5F, 3F, 25F, 3F, new CubeDeformation(grow))
                .texOffs(56, 46).addBox(-14F, -18F, -3F, 28F, 4F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        p_body.addOrReplaceChild("cage", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-6F, 0F, -4F, 12F, 12F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, -13F, 0F));
        p_body.addOrReplaceChild("heart", CubeListBuilder.create()
                .texOffs(24, 60).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(grow)),
            PartPose.offset(0F, -8F, 0F));
        p_body.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(12, 0).addBox(-12F, 0F, 0F, 24F, 26F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -16F, 3.2F, 0.1396F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-4.5F, -9F, -4.5F, 9F, 9F, 9F, new CubeDeformation(grow)),
            PartPose.offset(0F, -18F, 0F));
        p_head.addOrReplaceChild("head_earr", CubeListBuilder.create()
                .texOffs(8, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-4.5F, -7F, 0F, 0F, 0F, -0.384F));
        p_head.addOrReplaceChild("head_earl", CubeListBuilder.create()
                .texOffs(16, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(4.5F, -7F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_right_head = p_body.addOrReplaceChild("right_head", CubeListBuilder.create()
                .texOffs(60, 28).addBox(-3.5F, -7F, -3.5F, 7F, 7F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-11F, -17F, 0F, 0F, 0F, -0.1047F));
        p_right_head.addOrReplaceChild("right_head_earr", CubeListBuilder.create()
                .texOffs(24, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-3.5F, -5F, 0F, 0F, 0F, -0.384F));
        p_right_head.addOrReplaceChild("right_head_earl", CubeListBuilder.create()
                .texOffs(32, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(3.5F, -5F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_left_head = p_body.addOrReplaceChild("left_head", CubeListBuilder.create()
                .texOffs(88, 28).addBox(-3.5F, -7F, -3.5F, 7F, 7F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(11F, -17F, 0F, 0F, 0F, 0.1047F));
        p_left_head.addOrReplaceChild("left_head_earr", CubeListBuilder.create()
                .texOffs(40, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-3.5F, -5F, 0F, 0F, 0F, -0.384F));
        p_left_head.addOrReplaceChild("left_head_earl", CubeListBuilder.create()
                .texOffs(48, 46).addBox(-0.5F, -7F, -1.5F, 1F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(3.5F, -5F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(36, 28).addBox(-1.5F, -1F, -1.5F, 3F, 14F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-8.5F, -14F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(116, 28).addBox(-1F, 0F, -1F, 2F, 12F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 13F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(76, 60).addBox(-1.5F, 0F, -1.5F, 3F, 2F, 3F, new CubeDeformation(grow)),
            PartPose.offset(0F, 12F, 0F));
        p_right_hand.addOrReplaceChild("right_finger0", CubeListBuilder.create()
                .texOffs(52, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-1.5F, 2F, -0.5F, -0.1745F, 0F, -0.2094F));
        p_right_hand.addOrReplaceChild("right_finger1", CubeListBuilder.create()
                .texOffs(56, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 2F, -0.5F, -0.1745F, 0F, 0F));
        p_right_hand.addOrReplaceChild("right_finger2", CubeListBuilder.create()
                .texOffs(60, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1.5F, 2F, -0.5F, -0.1745F, 0F, 0.2094F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(48, 28).addBox(-1.5F, -1F, -1.5F, 3F, 14F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(8.5F, -14F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-1F, 0F, -1F, 2F, 12F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 13F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(88, 60).addBox(-1.5F, 0F, -1.5F, 3F, 2F, 3F, new CubeDeformation(grow)),
            PartPose.offset(0F, 12F, 0F));
        p_left_hand.addOrReplaceChild("left_finger0", CubeListBuilder.create()
                .texOffs(64, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-1.5F, 2F, -0.5F, -0.1745F, 0F, -0.2094F));
        p_left_hand.addOrReplaceChild("left_finger1", CubeListBuilder.create()
                .texOffs(68, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 2F, -0.5F, -0.1745F, 0F, 0F));
        p_left_hand.addOrReplaceChild("left_finger2", CubeListBuilder.create()
                .texOffs(72, 60).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1.5F, 2F, -0.5F, -0.1745F, 0F, 0.2094F));
        PartDefinition p_tendril0 = p_body.addOrReplaceChild("tendril0", CubeListBuilder.create()
                .texOffs(0, 60).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(grow)),
            PartPose.offset(-3F, 9F, 0F));
        p_tendril0.addOrReplaceChild("tendril0_tip", CubeListBuilder.create()
                .texOffs(40, 60).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril1 = p_body.addOrReplaceChild("tendril1", CubeListBuilder.create()
                .texOffs(8, 60).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(grow)),
            PartPose.offset(0F, 9F, 0.5F));
        p_tendril1.addOrReplaceChild("tendril1_tip", CubeListBuilder.create()
                .texOffs(44, 60).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril2 = p_body.addOrReplaceChild("tendril2", CubeListBuilder.create()
                .texOffs(16, 60).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(grow)),
            PartPose.offset(3F, 9F, 0F));
        p_tendril2.addOrReplaceChild("tendril2_tip", CubeListBuilder.create()
                .texOffs(48, 60).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 8F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    private float rightHeadY, rightHeadX, leftHeadY, leftHeadX;

    /** The side heads look wherever the boss's own head logic points them (like a wither's). */
    @Override
    public void prepareMobModel(Listener entity, float limbSwing, float limbSwingAmount, float partialTick) {
        rightHeadY = (entity.getHeadYRot(0) - entity.yBodyRot) * Mth.DEG_TO_RAD;
        rightHeadX = entity.getHeadXRot(0) * Mth.DEG_TO_RAD;
        leftHeadY = (entity.getHeadYRot(1) - entity.yBodyRot) * Mth.DEG_TO_RAD;
        leftHeadX = entity.getHeadXRot(1) * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(Listener entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        right_head.yRot += rightHeadY;
        right_head.xRot += rightHeadX;
        left_head.yRot += leftHeadY;
        left_head.xRot += leftHeadX;

        // It hangs in the air, breathing slowly; the heart pulses
        body.y += Mth.sin(ageInTicks * 0.06F) * 1.5F;
        mantle.xRot += Mth.sin(ageInTicks * 0.05F) * 0.06F + limbSwingAmount * 0.4F;
        float pulse = 1.0F + Mth.sin(ageInTicks * 0.25F) * 0.15F;
        heart.xScale = pulse; heart.yScale = pulse; heart.zScale = pulse;
        head_earr.zRot += Mth.sin(ageInTicks * 0.11F) * 0.08F;
        head_earl.zRot -= Mth.sin(ageInTicks * 0.11F) * 0.08F;

        // Long arms drifting, fingers curling
        right_arm.zRot += Mth.cos(ageInTicks * 0.05F) * 0.08F;
        left_arm.zRot -= Mth.cos(ageInTicks * 0.05F + 1.0F) * 0.08F;
        right_forearm.xRot += Mth.sin(ageInTicks * 0.07F) * 0.15F;
        left_forearm.xRot += Mth.sin(ageInTicks * 0.07F + 2.0F) * 0.15F;
        float curl = Mth.sin(ageInTicks * 0.09F) * 0.25F;
        right_finger0.xRot -= curl; right_finger1.xRot -= curl; right_finger2.xRot -= curl;
        left_finger0.xRot += curl; left_finger1.xRot += curl; left_finger2.xRot += curl;

        // Tendrils trail beneath it
        tendril0.xRot = Mth.sin(ageInTicks * 0.1F) * 0.25F + limbSwingAmount * 0.5F;
        tendril1.xRot = Mth.sin(ageInTicks * 0.1F + 1.0F) * 0.25F + limbSwingAmount * 0.5F;
        tendril2.xRot = Mth.sin(ageInTicks * 0.1F + 2.0F) * 0.25F + limbSwingAmount * 0.5F;
        tendril0.zRot = Mth.cos(ageInTicks * 0.08F) * 0.15F;
        tendril2.zRot = Mth.cos(ageInTicks * 0.08F + 2.0F) * 0.15F;
        tendril0_tip.xRot = Mth.sin(ageInTicks * 0.1F + 1.0F) * 0.35F;
        tendril1_tip.xRot = Mth.sin(ageInTicks * 0.1F + 2.0F) * 0.35F;
        tendril2_tip.xRot = Mth.sin(ageInTicks * 0.1F + 3.0F) * 0.35F;

    }
}
