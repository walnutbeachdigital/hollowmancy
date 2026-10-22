package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.AmethystColossus;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class ColossusGlowLayer extends EyesLayer<AmethystColossus, HumanoidModel<AmethystColossus>> {

    private static final RenderType GLOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/amethyst_colossus_glow.png"));

    public ColossusGlowLayer(RenderLayerParent<AmethystColossus, HumanoidModel<AmethystColossus>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}