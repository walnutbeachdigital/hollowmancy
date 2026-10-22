package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.entity.SpellcasterMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class CasterEyesLayer<T extends SpellcasterMob> extends EyesLayer<T, HumanoidModel<T>> {

    private final RenderType renderType;

    public CasterEyesLayer(RenderLayerParent<T, HumanoidModel<T>> parent, ResourceLocation glowTexture) {
        super(parent);
        this.renderType = RenderType.eyes(glowTexture);
    }

    @Override
    public RenderType renderType() {
        return renderType;
    }
}