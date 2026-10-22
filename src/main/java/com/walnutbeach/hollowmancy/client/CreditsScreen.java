package com.walnutbeach.hollowmancy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** The end of Hollowmancy: the story's last words, then the credits, rising up the screen. Esc skips. */
public class CreditsScreen extends Screen {

    /** The mod's author line. Change it to however you'd like to be credited. */
    public static final String AUTHOR = "Matthew Porter";

    private static final List<String> LINES = List.of(
        "#HOLLOWMANCY", "", "", "",
        "The Hollow is silent.", "",
        "The thing that listened is gone,", "and the veil is only a veil again.", "", "",
        "The Circle crossed it first. Seven of them.",
        "Vael carried what they could not,", "until it unmade him.", "",
        "Morvane kept his oath to the very end.", "",
        "The Colossus sang, and then it did not.", "",
        "The Leviathan's song ended in the deep.", "", "",
        "You walked every step they walked,", "and one step further.", "", "",
        "#You are an Archmage.", "", "", "", "",
        "~Hollowmancy", "", "a mod by", "#" + AUTHOR, "", "", "",
        "~With thanks to", "", "NeoForge", "Patchouli", "Just Enough Items", "", "", "",
        "Thank you for playing.", "", "", "", "",
        "*The world is still there. So is your magic.");

    private int ticks = 0;

    public CreditsScreen() {
        super(Component.translatable("hollowmancy.credits_screen.credits"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new CreditsScreen());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF07040C);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int y = (int) (height - (ticks + partialTick) * 1.0f);   // one pixel per tick: about a minute in all
        for (String line : LINES) {
            int color = 0xCFC6DA;
            String text = line;
            float size = 1.0f;
            if (line.startsWith("#")) { text = line.substring(1); color = 0xFFD75E; size = 2.0f; }
            else if (line.startsWith("~")) { text = line.substring(1); color = 0x9B5DE5; }
            else if (line.startsWith("*")) { text = line.substring(1); color = 0x8080A0; }
            if (y > -30 && y < height + 10) {
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2.0f, y, 0);
                graphics.pose().scale(size, size, 1.0f);
                graphics.drawCenteredString(font, text, 0, 0, color);
                graphics.pose().popPose();
            }
            y += size > 1 ? 24 : 12;
        }
        if (y < -20) onClose();   // reached the end
        graphics.drawString(font, Component.translatable("hollowmancy.credits_screen.esc_to_skip"), width - font.width("Esc to skip") - 6, height - 12, 0x505060);
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
