package com.walnutbeach.hollowmancy.client.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.familiar.OwlFamiliar;
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

public class OwlModel extends HierarchicalModel<OwlFamiliar> {

    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "owl_familiar"), "main");

    private final ModelPart root, body, head, leftWing, rightWing;

    public OwlModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 0, -2.5f, 6, 7, 5), PartPose.offset(0, 14, 0));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3, -5, -3, 6, 5, 6)
                .texOffs(24, 12).addBox(-3, -7, -1, 1, 2, 1)
                .texOffs(24, 12).addBox(2, -7, -1, 1, 2, 1)
                .texOffs(28, 0).addBox(-0.5f, -2.5f, -4, 1, 2, 1),
            PartPose.offset(0, 14, 0));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(36, 0).addBox(0, 0, -2, 1, 6, 4), PartPose.offset(3, 14.5f, 0));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-1, 0, -2, 1, 6, 4), PartPose.offset(-3, 14.5f, 0));
        root.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(28, 4).addBox(-0.5f, 0, -1, 1, 2, 1), PartPose.offset(1.5f, 21, 0));
        root.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(28, 4).addBox(-0.5f, 0, -1, 1, 2, 1), PartPose.offset(-1.5f, 21, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(OwlFamiliar owl, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        boolean sitting = owl.isOrderedToSit();
        float speed = sitting ? 0.15f : 0.55f;
        float flap = Mth.sin(ageInTicks * speed) * (sitting ? 0.15f : 0.7f);
        leftWing.zRot = -0.2f - flap;
        rightWing.zRot = 0.2f + flap;
        float bob = Mth.sin(ageInTicks * 0.1f) * 0.6f;
        body.y = 14 + bob;
        head.y = 14 + bob;
        leftWing.y = 14.5f + bob;
        rightWing.y = 14.5f + bob;
    }
}
