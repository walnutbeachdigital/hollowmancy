package com.walnutbeach.hollowmancy.client.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.familiar.GolemFamiliar;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GolemModel extends HierarchicalModel<GolemFamiliar> {

    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "golem_familiar"), "main");

    private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;

    public GolemModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 0, -3, 8, 7, 6), PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-3, -6, -3, 6, 6, 6)
                .texOffs(24, 13).addBox(-1, -9, -1, 2, 3, 2),
            PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5f, 0, -1.5f, 3, 9, 3), PartPose.offset(-5.5f, 13.5f, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-1.5f, 0, -1.5f, 3, 9, 3), PartPose.offset(5.5f, 13.5f, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 25).addBox(-1.5f, 0, -1.5f, 3, 4, 3), PartPose.offset(-2, 20, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-1.5f, 0, -1.5f, 3, 4, 3), PartPose.offset(2, 20, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(GolemFamiliar golem, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float walk = Mth.cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount;
        rightLeg.xRot = walk;
        leftLeg.xRot = -walk;
        rightArm.xRot = -walk * 0.8f;
        leftArm.xRot = walk * 0.8f;

        float attack = golem.getAttackAnim(ageInTicks - golem.tickCount);
        if (attack > 0) {
            float swing = Mth.sin(attack * Mth.PI);
            rightArm.xRot = -1.8f * swing;
            leftArm.xRot = -1.8f * swing;
        }
        if (golem.isOrderedToSit()) {
            rightLeg.xRot = -1.4f;
            leftLeg.xRot = -1.4f;
        }
    }
}
