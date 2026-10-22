package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.AmethystColossus;
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
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/amethyst_colossus.bbmodel).
 * Texture: textures/entity/amethyst_colossus.png (128x128), glow: textures/entity/amethyst_colossus_glow.png.
 */
public class AmethystColossusModel extends HierarchicalModel<AmethystColossus> {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "amethyst_colossus"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart right_arm;
    private final ModelPart right_forearm;
    private final ModelPart left_arm;
    private final ModelPart left_forearm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public AmethystColossusModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.right_arm = this.chest.getChild("right_arm");
        this.right_forearm = this.right_arm.getChild("right_forearm");
        this.left_arm = this.chest.getChild("left_arm");
        this.left_forearm = this.left_arm.getChild("left_forearm");
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
                .texOffs(30, 76).addBox(-7F, -4F, -4F, 14F, 5F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 7F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -18F, -6F, 18F, 18F, 12F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -3F, 0F, 0.2793F, 0F, 0F));
        p_chest.addOrReplaceChild("crystal0", CubeListBuilder.create()
                .texOffs(74, 76).addBox(-1.5F, -9F, -1.5F, 3F, 9F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5F, -18F, 3F, -0.4363F, 0F, -0.3491F));
        p_chest.addOrReplaceChild("crystal1", CubeListBuilder.create()
                .texOffs(32, 55).addBox(-2F, -12F, -2F, 4F, 12F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -18F, 2F, -0.6109F, 0F, 0.0873F));
        p_chest.addOrReplaceChild("crystal2", CubeListBuilder.create()
                .texOffs(32, 90).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5F, -18F, 3F, -0.3491F, 0F, 0.4363F));
        p_chest.addOrReplaceChild("crystal3", CubeListBuilder.create()
                .texOffs(80, 90).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-8F, -14F, 5F, -1.0472F, 0F, -0.5236F));
        p_chest.addOrReplaceChild("crystal4", CubeListBuilder.create()
                .texOffs(88, 90).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(7F, -13F, 5F, -1.0472F, 0F, 0.6109F));
        p_chest.addOrReplaceChild("crystal5", CubeListBuilder.create()
                .texOffs(44, 90).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1F, -9F, 6F, -1.309F, 0F, 0F));
        p_chest.addOrReplaceChild("crystal6", CubeListBuilder.create()
                .texOffs(96, 90).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-3F, -5F, 6F, -1.3963F, 0F, -0.1745F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-4F, -5F, -7F, 8F, 7F, 7F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -16F, -6F, -0.2793F, 0F, 0F));
        p_head.addOrReplaceChild("brow_crystal", CubeListBuilder.create()
                .texOffs(104, 90).addBox(-1.5F, -4F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -5F, -4F, -0.3491F, 0F, 0F));
        PartDefinition p_right_arm = p_chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(96, 30).addBox(-4F, -3F, -4F, 8F, 13F, 8F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-13F, -14F, -1F, -0.2793F, 0F, -0.1396F));
        p_right_arm.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
                .texOffs(48, 55).addBox(-5F, -3F, -5F, 10F, 5F, 10F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-5F, 0F, -5F, 10F, 15F, 10F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 9F, 0F, -0.3142F, 0F, 0.1047F));
        p_right_forearm.addOrReplaceChild("right_spike0", CubeListBuilder.create()
                .texOffs(56, 90).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(4F, 4F, 0F, 0F, 0F, 0.9599F));
        p_right_forearm.addOrReplaceChild("right_spike1", CubeListBuilder.create()
                .texOffs(116, 90).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(3F, 9F, -3F, -0.6981F, 0F, 0.6981F));
        p_right_forearm.addOrReplaceChild("right_spike2", CubeListBuilder.create()
                .texOffs(0, 102).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 2F, 4F, 1.0472F, 0F, 0F));
        PartDefinition p_left_arm = p_chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-4F, -3F, -4F, 8F, 13F, 8F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(13F, -14F, -1F, -0.2793F, 0F, 0.1396F));
        p_left_arm.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
                .texOffs(88, 55).addBox(-5F, -3F, -5F, 10F, 5F, 10F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0.1396F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-5F, 0F, -5F, 10F, 15F, 10F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 9F, 0F, -0.3142F, 0F, -0.1047F));
        p_left_forearm.addOrReplaceChild("left_spike0", CubeListBuilder.create()
                .texOffs(68, 90).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-4F, 4F, 0F, 0F, 0F, -0.9599F));
        p_left_forearm.addOrReplaceChild("left_spike1", CubeListBuilder.create()
                .texOffs(8, 102).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-3F, 9F, -3F, -0.6981F, 0F, -0.6981F));
        p_left_forearm.addOrReplaceChild("left_spike2", CubeListBuilder.create()
                .texOffs(16, 102).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 2F, 4F, 1.0472F, 0F, 0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(40, 30).addBox(-3.5F, 0F, -3.5F, 7F, 16F, 7F, new CubeDeformation(grow))
                .texOffs(86, 76).addBox(-4F, 12F, -4.5F, 8F, 4F, 8F, new CubeDeformation(grow)),
            PartPose.offset(-5F, 8F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(68, 30).addBox(-3.5F, 0F, -3.5F, 7F, 16F, 7F, new CubeDeformation(grow))
                .texOffs(0, 90).addBox(-4F, 12F, -4.5F, 8F, 4F, 8F, new CubeDeformation(grow)),
            PartPose.offset(5F, 8F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AmethystColossus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.5F;
        head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
        float walk = limbSwing * 0.5F;
        right_leg.xRot = Mth.cos(walk) * 0.8F * limbSwingAmount;
        left_leg.xRot = Mth.cos(walk + Mth.PI) * 0.8F * limbSwingAmount;
        right_arm.xRot += Mth.cos(walk + Mth.PI) * 0.45F * limbSwingAmount;
        left_arm.xRot += Mth.cos(walk) * 0.45F * limbSwingAmount;

        // Every heavy step rolls the whole body
        body.zRot = Mth.cos(limbSwing * 0.5F) * 0.07F * limbSwingAmount;
        chest.xRot += Mth.sin(ageInTicks * 0.05F) * 0.02F;
        right_forearm.xRot -= Mth.sin(ageInTicks * 0.05F) * 0.04F;
        left_forearm.xRot -= Mth.cos(ageInTicks * 0.05F) * 0.04F;

        // Both fists raised, then brought down together
        if (attackTime > 0.0F) {
            boolean rising = attackTime < 0.45F;
            float lift = rising ? attackTime / 0.45F : 1.0F - (attackTime - 0.45F) / 0.55F;
            float arms = rising ? -2.9F * lift : Mth.lerp((attackTime - 0.45F) / 0.55F, -2.9F, 0.35F);
            right_arm.xRot += arms;
            left_arm.xRot += arms;
            right_forearm.xRot -= 0.5F * lift;
            left_forearm.xRot -= 0.5F * lift;
            chest.xRot -= 0.3F * lift;
        }

    }
}
