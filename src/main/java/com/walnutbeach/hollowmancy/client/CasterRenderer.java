package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.client.model.CasterModel;
import com.walnutbeach.hollowmancy.entity.SpellcasterMob;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a spellcaster with its own model. The model is picked from the texture's name
 * (tower_wizard, fire_wizard, frost_wizard, void_wizard, hollow_cultist, rogue_apprentice).
 */
public class CasterRenderer<T extends SpellcasterMob> extends MobRenderer<T, CasterModel<T>> {

    private final ResourceLocation texture;

    /** glowTexture may be null for casters without glowing parts. */
    public CasterRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation glowTexture) {
        super(context, new CasterModel<>(context.bakeLayer(CasterModel.layer(kindOf(texture))), kindOf(texture)), 0.5f);
        this.texture = texture;
        if (glowTexture != null) {
            RenderType glow = RenderType.eyes(glowTexture);
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return glow;
                }
            });
        }
    }

    private static String kindOf(ResourceLocation texture) {
        String path = texture.getPath();
        for (String kind : CasterModel.KINDS) {
            if (path.contains(kind)) return kind;
        }
        return "tower_wizard";
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
