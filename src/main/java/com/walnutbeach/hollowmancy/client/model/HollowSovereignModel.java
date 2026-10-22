package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.HollowSovereign;
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
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/hollow_sovereign.bbmodel).
 * Texture: textures/entity/hollow_sovereign.png (128x128), glow: textures/entity/hollow_sovereign_glow.png.
 */
public class HollowSovereignModel extends HierarchicalModel<HollowSovereign> {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "hollow_sovereign"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart head;
    private final ModelPart halo;
    private final ModelPart right_arm;
    private final ModelPart right_hand;
    private final ModelPart right_claw;
    private final ModelPart left_arm;
    private final ModelPart left_hand;
    private final ModelPart left_claw;

    public HollowSovereignModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.skirt = this.body.getChild("skirt");
        this.head = this.body.getChild("head");
        this.halo = this.body.getChild("halo");
        this.right_arm = this.body.getChild("right_arm");
        this.right_hand = this.right_arm.getChild("right_hand");
        this.right_claw = this.right_hand.getChild("right_claw");
        this.left_arm = this.body.getChild("left_arm");
        this.left_hand = this.left_arm.getChild("left_hand");
        this.left_claw = this.left_hand.getChild("left_claw");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(0.0F);
    }

    /** grow: puffs every cube out (used for the energy shell). */
    public static LayerDefinition createBodyLayer(float grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(),
            PartPose.offset(0F, 0F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, 0F, -4F, 12F, 18F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 3F, 0F));
        p_body.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-5F, -9F, -3F, 10F, 12F, 6F, new CubeDeformation(grow))
                .texOffs(76, 26).addBox(-6F, -12F, -4F, 12F, 4F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(16, 26).addBox(-3F, -7F, -3F, 6F, 7F, 6F, new CubeDeformation(grow))
                .texOffs(72, 0).addBox(-4F, -8.5F, -4F, 8F, 9F, 8F, new CubeDeformation(0.2F + grow)),
            PartPose.offset(0F, -11F, 0F));
        p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(40, 26).addBox(-4.5F, -4F, -4.5F, 9F, 4F, 9F, new CubeDeformation(0.3F + grow)),
            PartPose.offset(0F, -8.5F, 0F));
        PartDefinition p_halo = p_body.addOrReplaceChild("halo", CubeListBuilder.create(),
            PartPose.offset(0F, -15F, 6F));
        p_halo.addOrReplaceChild("shard0", CubeListBuilder.create()
                .texOffs(64, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(7F, 0F, 0F, 0F, 0F, 1.5708F));
        p_halo.addOrReplaceChild("shard1", CubeListBuilder.create()
                .texOffs(70, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(4.9497F, 4.9497F, 0F, 0F, 0F, 2.3562F));
        p_halo.addOrReplaceChild("shard2", CubeListBuilder.create()
                .texOffs(76, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 7F, 0F, 0F, 0F, 3.1416F));
        p_halo.addOrReplaceChild("shard3", CubeListBuilder.create()
                .texOffs(82, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-4.9497F, 4.9497F, 0F, 0F, 0F, 3.927F));
        p_halo.addOrReplaceChild("shard4", CubeListBuilder.create()
                .texOffs(88, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-7F, 0F, 0F, 0F, 0F, 4.7124F));
        p_halo.addOrReplaceChild("shard5", CubeListBuilder.create()
                .texOffs(94, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-4.9497F, -4.9497F, 0F, 0F, 0F, 5.4978F));
        p_halo.addOrReplaceChild("shard6", CubeListBuilder.create()
                .texOffs(100, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 6.2832F));
        p_halo.addOrReplaceChild("shard7", CubeListBuilder.create()
                .texOffs(106, 41).addBox(-1F, -2F, -0.5F, 2F, 4F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(4.9497F, -4.9497F, 0F, 0F, 0F, 7.0686F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-2F, -1.5F, -2F, 4F, 11F, 4F, new CubeDeformation(grow))
                .texOffs(0, 41).addBox(-2.5F, 7F, -2.5F, 5F, 5F, 5F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-6.5F, -8F, 0F, 0F, 0F, 0.1396F));
        PartDefinition p_right_hand = p_right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(40, 41).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        p_right_hand.addOrReplaceChild("right_claw", CubeListBuilder.create()
                .texOffs(112, 41).addBox(-1.5F, 0F, -1F, 3F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 4F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-2F, -1.5F, -2F, 4F, 11F, 4F, new CubeDeformation(grow))
                .texOffs(20, 41).addBox(-2.5F, 7F, -2.5F, 5F, 5F, 5F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(6.5F, -8F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_left_hand = p_left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(52, 41).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        p_left_hand.addOrReplaceChild("left_claw", CubeListBuilder.create()
                .texOffs(120, 41).addBox(-1.5F, 0F, -1F, 3F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 4F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(HollowSovereign entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;

        // It floats, and its robe drifts behind it
        body.y += Mth.sin(ageInTicks * 0.07F) * 1.2F;
        skirt.xRot += limbSwingAmount * 0.35F + Mth.sin(ageInTicks * 0.07F + 1.0F) * 0.05F;
        float idle = Mth.cos(ageInTicks * 0.06F) * 0.08F;
        right_arm.zRot += idle;
        left_arm.zRot -= idle;
        right_arm.xRot += Mth.cos(limbSwing * 0.5F + Mth.PI) * 0.3F * limbSwingAmount;
        left_arm.xRot += Mth.cos(limbSwing * 0.5F) * 0.3F * limbSwingAmount;
        halo.zRot = ageInTicks * 0.04F;

        // Channelling: hands raised, the halo spins faster
        if (entity.isAggressive()) {
            right_arm.xRot = -1.3F + Mth.sin(ageInTicks * 0.3F) * 0.1F;
            left_arm.xRot = -1.3F + Mth.cos(ageInTicks * 0.3F) * 0.1F;
            right_arm.yRot = -0.3F;
            left_arm.yRot = 0.3F;
            right_hand.xRot = -0.5F;
            left_hand.xRot = -0.5F;
            halo.zRot = ageInTicks * 0.12F;
        }
        right_claw.xRot = Mth.sin(ageInTicks * 0.2F) * 0.2F;
        left_claw.xRot = Mth.cos(ageInTicks * 0.2F) * 0.2F;

        if (attackTime > 0.0F) {
            float thrust = Mth.sin(attackTime * Mth.PI);
            right_arm.xRot = -2.0F * thrust;
            left_arm.xRot = -2.0F * thrust;
        }

    }
}
