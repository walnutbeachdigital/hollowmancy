package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * The spellbook in your hand stays shut, swings open when you open the spellbook screen,
 * and closes again when you leave it. Drives the "hollowmancy:open" model property
 * (0 = shut, 1 = open) used by models/item/spellbook.json.
 * Uses real time, not game ticks, because single-player pauses while a screen is open.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SpellbookOpening {
    /** How long it takes to open or close, in milliseconds. */
    private static final float DURATION_MS = 500.0F;

    private static boolean open;
    private static float from, current;
    private static long changedAt;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item book = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "spellbook"));
            if (book == Items.AIR) return;
            ItemProperties.register(book, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "open"),
                (stack, level, entity, seed) -> openness(entity));
        });
    }

    /** How open the book in your hand is right now (0 = shut, 1 = open), for the arm to follow. */
    public static float openness() {
        return current;
    }

    private static float openness(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == null || entity != mc.player) return 0.0F;   // other players' books, item frames, the ground
        boolean wantOpen = mc.screen instanceof SpellbookScreen;
        long now = Util.getMillis();
        if (wantOpen != open) {
            open = wantOpen;
            from = current;
            changedAt = now;
        }
        float t = Mth.clamp((now - changedAt) / DURATION_MS, 0.0F, 1.0F);
        t = t * t * (3.0F - 2.0F * t);   // ease in and out
        current = Mth.lerp(t, from, wantOpen ? 1.0F : 0.0F);
        return current;
    }
}
