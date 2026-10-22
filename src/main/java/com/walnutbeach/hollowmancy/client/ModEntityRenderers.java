package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOLLOW_SOVEREIGN.get(), SovereignRenderer::new);
        event.registerEntityRenderer(ModEntities.AMETHYST_COLOSSUS.get(), ColossusRenderer::new);
        event.registerEntityRenderer(ModEntities.REVENANT_KING.get(), KingRenderer::new);
        event.registerEntityRenderer(ModEntities.CASTELLAN.get(), CastellanRenderer::new);
        event.registerEntityRenderer(ModEntities.LEVIATHAN.get(), LeviathanRenderer::new);
        event.registerEntityRenderer(ModEntities.LISTENER.get(), ListenerRenderer::new);
        event.registerEntityRenderer(ModEntities.HOLY_WATER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.ROGUE_APPRENTICE.get(),
                context -> new CasterRenderer<>(context, texture("rogue_apprentice"), null));
        event.registerEntityRenderer(ModEntities.HOLLOW_CULTIST.get(),
                context -> new CasterRenderer<>(context, texture("hollow_cultist"), texture("hollow_cultist_eyes")));
        event.registerEntityRenderer(ModEntities.TOWER_WIZARD.get(),
            context -> new CasterRenderer<>(context, texture("tower_wizard"), texture("tower_wizard_eyes")));
        event.registerEntityRenderer(ModEntities.FIRE_WIZARD.get(),
            context -> new CasterRenderer<>(context, texture("fire_wizard"), texture("fire_wizard_eyes")));
        event.registerEntityRenderer(ModEntities.FROST_WIZARD.get(),
            context -> new CasterRenderer<>(context, texture("frost_wizard"), texture("frost_wizard_eyes")));
        event.registerEntityRenderer(ModEntities.VOID_WIZARD.get(),
            context -> new CasterRenderer<>(context, texture("void_wizard"), texture("void_wizard_eyes")));
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/" + name + ".png");
    }
}