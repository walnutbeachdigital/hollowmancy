package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.HollowSovereign;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class SovereignEyesLayer extends EyesLayer<HollowSovereign, HumanoidModel<HollowSovereign>> {

    private static final RenderType EYES = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/hollow_sovereign_eyes.png"));

    public SovereignEyesLayer(RenderLayerParent<HollowSovereign, HumanoidModel<HollowSovereign>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}