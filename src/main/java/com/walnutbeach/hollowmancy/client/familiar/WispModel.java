package com.walnutbeach.hollowmancy.client.familiar;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.familiar.WispFamiliar;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WispModel extends HierarchicalModel<WispFamiliar> {

    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "wisp_familiar"), "main");

    private final ModelPart root, core;
    private final ModelPart[] motes = new ModelPart[3];

    public WispModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.core = root.getChild("core");
        for (int i = 0; i < motes.length; i++) motes[i] = root.getChild("mote" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -3, -3, 6, 6, 6), PartPose.offset(0, 18, 0));
        for (int i = 0; i < 3; i++) {
            root.addOrReplaceChild("mote" + i, CubeListBuilder.create().texOffs(0, 12).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(0, 18, 0));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(WispFamiliar wisp, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = Mth.sin(ageInTicks * 0.1f);
        core.y = 18 + bob;
        core.yRot = ageInTicks * 0.05f;
        core.xRot = ageInTicks * 0.03f;
        for (int i = 0; i < motes.length; i++) {
            float angle = ageInTicks * 0.15f + i * Mth.TWO_PI / 3;
            motes[i].x = Mth.cos(angle) * 5.5f;
            motes[i].z = Mth.sin(angle) * 5.5f;
            motes[i].y = 18 + bob + Mth.sin(ageInTicks * 0.2f + i * 2) * 2.0f;
            motes[i].yRot = -angle;
        }
    }
}
