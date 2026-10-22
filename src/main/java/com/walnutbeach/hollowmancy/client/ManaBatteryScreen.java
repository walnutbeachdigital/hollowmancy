package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.menu.ManaBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

public class ManaBatteryScreen extends AbstractContainerScreen<ManaBatteryMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/gui/mana_battery.png");

    // The gauge's inner area in the texture
    private static final int GAUGE_X = 12;
    private static final int GAUGE_Y = 25;
    private static final int GAUGE_W = 12;
    private static final int GAUGE_H = 60;

    private static final String[] LABELS = {"+10", "+50", "+All", "-10", "-50", "-All"};
    private final Button[] buttons = new Button[6];

    public ManaBatteryScreen(ManaBatteryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 196;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 6; i++) {
            int id = i;
            int row = i / 3;
            int col = i % 3;
            buttons[i] = addRenderableWidget(Button.builder(Component.literal(LABELS[i]),
                            b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                    .bounds(leftPos + 34 + col * 26, topPos + 54 + row * 18, 24, 14)
                    .tooltip(Tooltip.create((i < 3 ? Component.translatable("hollowmancy.mana_battery_screen.store_your_mana_in_the_battery") : Component.translatable("hollowmancy.mana_battery_screen.draw_mana_from_the_battery"))))
                    .build());
        }
    }

    // Grey out buttons that can't do anything right now
    @Override
    protected void containerTick() {
        super.containerTick();
        MagicData data = minecraft.player.getData(ModAttachments.MAGIC);
        boolean canStore = data.mana() > 0 && menu.getMana() < menu.getCapacity();
        boolean canDraw = menu.getMana() > 0 && data.mana() < data.maxMana();
        for (int i = 0; i < 6; i++) buttons[i].active = i < 3 ? canStore : canDraw;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Gauge fills from the bottom
        int capacity = Math.max(1, menu.getCapacity());
        int filled = Math.round((float) GAUGE_H * menu.getMana() / capacity);
        int x = leftPos + GAUGE_X;
        int bottom = topPos + GAUGE_Y + GAUGE_H;
        if (filled > 0) {
            // The animated mana texture, tiled from the top of the gauge and clipped to the filled part
            TextureAtlasSprite swirl = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ManaBatteryRenderer.SWIRL);
            graphics.enableScissor(x, bottom - filled, x + GAUGE_W, bottom);
            for (int ty = bottom - GAUGE_H; ty < bottom; ty += 16) graphics.blit(x, ty, 0, 16, 16, swirl);
            graphics.disableScissor();
            graphics.fill(x, bottom - filled, x + GAUGE_W, bottom - filled + 1, 0xFFE8DEFF); // bright surface line
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Light text for the dark screen, like the Research Table
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE8D8FF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFB8A8D0, false);
        MagicData data = minecraft.player.getData(ModAttachments.MAGIC);
        graphics.drawString(font, Component.translatable("hollowmancy.mana_battery_screen.amount", String.format("%,d", menu.getMana()), String.format("%,d", menu.getCapacity())), 34, 25, 0xFFE8D8FF, false);
        graphics.drawString(font, Component.translatable("hollowmancy.mana_battery_screen.you", data.mana(), data.maxMana()), 34, 37, 0xFFA98BD8, false);

        String charge = "Charge";
        String drain = "Drain";
        graphics.drawString(font, charge, 144 - font.width(charge) / 2, 18, 0xFFE8D8FF, false);
        graphics.drawString(font, drain, 144 - font.width(drain) / 2, 58, 0xFFE8D8FF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // Exact numbers when hovering the gauge
        int gx = leftPos + GAUGE_X;
        int gy = topPos + GAUGE_Y;
        if (mouseX >= gx && mouseX < gx + GAUGE_W && mouseY >= gy && mouseY < gy + GAUGE_H) {
            graphics.renderTooltip(font, Component.translatable("hollowmancy.mana_battery_screen.mana", menu.getMana(), menu.getCapacity()), mouseX, mouseY);
        }
    }
}