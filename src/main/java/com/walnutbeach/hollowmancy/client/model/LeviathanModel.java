package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walnutbeach.hollowmancy.entity.Leviathan;
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
 * Generated from the Hollowmancy boss model generator (Blockbench project: blockbench/leviathan.bbmodel).
 * Texture: textures/entity/leviathan.png (128x128), glow: textures/entity/leviathan_glow.png.
 */
public class LeviathanModel extends HierarchicalModel<Leviathan> {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "leviathan"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart jaw;
    private final ModelPart right_fin;
    private final ModelPart left_fin;
    private final ModelPart spike0;
    private final ModelPart spike1;
    private final ModelPart spike2;
    private final ModelPart spike3;
    private final ModelPart spike4;
    private final ModelPart spike5;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart fluke;

    public LeviathanModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.jaw = this.body.getChild("head").getChild("jaw");
        this.right_fin = this.body.getChild("right_fin");
        this.left_fin = this.body.getChild("left_fin");
        this.spike0 = this.body.getChild("spike0");
        this.spike1 = this.body.getChild("spike1");
        this.spike2 = this.body.getChild("spike2");
        this.spike3 = this.body.getChild("spike3");
        this.spike4 = this.body.getChild("spike4");
        this.spike5 = this.body.getChild("spike5");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.fluke = this.tail3.getChild("fluke");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(0.0F);
    }

    /** grow: puffs every cube out (used for the energy shell). */
    public static LayerDefinition createBodyLayer(float grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -6F, -9F, 14F, 12F, 18F, new CubeDeformation(grow))
                .texOffs(0, 30).addBox(-6F, 5F, -8F, 12F, 2F, 16F, new CubeDeformation(grow))
                .texOffs(56, 30).addBox(-1F, -9F, -7F, 2F, 3F, 14F, new CubeDeformation(grow)),
            PartPose.offset(0F, 14F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(88, 30).addBox(-6F, -5F, -8F, 12F, 8F, 8F, new CubeDeformation(grow))
                .texOffs(0, 72).addBox(-2.5F, -4F, -8.6F, 5F, 5F, 1F, new CubeDeformation(grow))
                .texOffs(12, 72).addBox(-5F, -7F, -6F, 2F, 3F, 2F, new CubeDeformation(grow))
                .texOffs(20, 72).addBox(3F, -7F, -6F, 2F, 3F, 2F, new CubeDeformation(grow)),
            PartPose.offset(0F, -1F, -9F));
        p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(56, 48).addBox(-5.5F, 0F, -7.5F, 11F, 3F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 3F, 0F));
        p_body.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(94, 48).addBox(-9F, -0.5F, 0F, 9F, 1F, 8F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-7F, 2F, -3F, 0F, 0F, -0.4363F));
        p_body.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(0, 63).addBox(0F, -0.5F, 0F, 9F, 1F, 8F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(7F, 2F, -3F, 0F, 0F, 0.4363F));
        p_body.addOrReplaceChild("spike0", CubeListBuilder.create()
                .texOffs(34, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offset(0F, -6F, -3F));
        p_body.addOrReplaceChild("spike1", CubeListBuilder.create()
                .texOffs(42, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-6F, -5F, 3F, 0F, 0F, -0.7854F));
        p_body.addOrReplaceChild("spike2", CubeListBuilder.create()
                .texOffs(50, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(6F, -5F, 3F, 0F, 0F, 0.7854F));
        p_body.addOrReplaceChild("spike3", CubeListBuilder.create()
                .texOffs(58, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-7F, 2F, -5F, 0F, 0F, -1.5708F));
        p_body.addOrReplaceChild("spike4", CubeListBuilder.create()
                .texOffs(66, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(7F, 2F, -5F, 0F, 0F, 1.5708F));
        p_body.addOrReplaceChild("spike5", CubeListBuilder.create()
                .texOffs(74, 63).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(grow)),
            PartPose.offset(0F, -6F, 5F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-5F, -4.5F, 0F, 10F, 9F, 10F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 9F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-3.5F, -3F, 0F, 7F, 6F, 9F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 10F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-2F, -2F, 0F, 4F, 4F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 9F));
        p_tail3.addOrReplaceChild("fluke", CubeListBuilder.create()
                .texOffs(82, 63).addBox(-7F, -0.5F, 0F, 14F, 1F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 7F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Leviathan entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        float partialTick = ageInTicks - entity.tickCount;
        body.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        body.xRot = headPitch * Mth.DEG_TO_RAD;

        // The tail sweeps like a guardian's, only longer
        float tail = entity.getTailAnimation(partialTick);
        tail1.yRot = Mth.sin(tail) * Mth.PI * 0.05F;
        tail2.yRot = Mth.sin(tail) * Mth.PI * 0.1F;
        tail3.yRot = Mth.sin(tail) * Mth.PI * 0.15F;
        fluke.yRot = Mth.sin(tail) * Mth.PI * 0.1F;
        tail2.xRot = Mth.sin(ageInTicks * 0.08F) * 0.06F;

        // Spines bristle when it's on the move
        float spikes = (1.0F - entity.getSpikesAnimation(partialTick)) * 1.4F;
        float length = 0.45F + spikes;
        spike0.yScale = length; spike1.yScale = length; spike2.yScale = length;
        spike3.yScale = length; spike4.yScale = length; spike5.yScale = length;

        right_fin.zRot += Mth.sin(ageInTicks * 0.15F) * 0.25F;
        left_fin.zRot -= Mth.sin(ageInTicks * 0.15F) * 0.25F;

        // Its jaw opens while the laser charges
        jaw.xRot = entity.hasActiveAttackTarget()
            ? 0.5F + Mth.sin(ageInTicks * 0.6F) * 0.05F
            : 0.06F + Mth.sin(ageInTicks * 0.05F) * 0.05F;

    }
}
