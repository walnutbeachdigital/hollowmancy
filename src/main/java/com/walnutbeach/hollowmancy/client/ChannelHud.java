package com.walnutbeach.hollowmancy.client;

import net.minecraft.network.chat.Component;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.spell.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChannelHud {

    private static final int BAR_WIDTH = 100;

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "channel_bar"),
                ChannelHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        Spell spell = ClientSpellState.getChannelSpell();
        if (spell == null) return;

        float progress = ClientSpellState.channelProgress(mc.level.getGameTime(),
                deltaTracker.getGameTimeDeltaPartialTick(true));
        int color = 0xFF000000 | spell.getSchool().getColor();

        int centerX = graphics.guiWidth() / 2;
        int x = centerX - BAR_WIDTH / 2;
        int y = graphics.guiHeight() / 2 + 16;

        graphics.blit(spell.getIcon(), x - 20, y - 6, 0, 0, 16, 16, 16, 16);

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + 5, 0xFF000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + 4, 0xFF1A1A1A);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * progress), y + 4, color);

        graphics.drawCenteredString(mc.font, Component.translatable("hollowmancy.channel_hud.channeling", spell.getName()), centerX, y + 8, color);
    }
}