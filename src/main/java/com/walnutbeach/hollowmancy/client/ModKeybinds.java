package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeybinds {

    private static final String CATEGORY = "key.categories.hollowmancy";

    public static final KeyMapping CAST_SPELL =
            new KeyMapping("key.hollowmancy.cast_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping CYCLE_SPELL =
            new KeyMapping("key.hollowmancy.cycle_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping OPEN_SPELLBOOK =
            new KeyMapping("key.hollowmancy.open_spellbook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CAST_SPELL);
        event.register(CYCLE_SPELL);
        event.register(OPEN_SPELLBOOK);
    }
}