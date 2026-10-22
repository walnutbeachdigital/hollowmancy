package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Draws textures/entity/<name>_glow.png at full brightness: eyes, crystals, runes. */
public class BossGlowLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType type;

    public BossGlowLayer(RenderLayerParent<T, M> parent, String name) {
        super(parent);
        this.type = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/" + name + "_glow.png"));
    }

    @Override
    public RenderType renderType() {
        return type;
    }
}
