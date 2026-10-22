package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.menu.ResearchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

/**
 * The Research Table: Dull Shards in the slot, a mana gauge showing what you have against what studying costs,
 * a pip for each shard needed, and your Knowledge.
 */
public class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/gui/research_table.png");
    private static final ResourceLocation SWIRL = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "block/mana_swirl");

    // Where things sit (inside the 176x166 panel)
    private static final int GAUGE_X = 62, GAUGE_Y = 57, GAUGE_W = 100, GAUGE_H = 6;
    private static final int PIPS_X = 22, PIPS_Y = 58;

    private Button studyButton;
    private float shownMana = -1;

    public ResearchScreen(ResearchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        studyButton = addRenderableWidget(Button.builder(Component.translatable("hollowmancy.research_screen.study"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ResearchMenu.STUDY_BUTTON))
                .bounds(leftPos + 62, topPos + 33, 100, 20)
                .build());
    }

    // Grey out the button (with a reason) when the player can't study
    @Override
    protected void containerTick() {
        super.containerTick();
        MagicData data = minecraft.player.getData(ModAttachments.MAGIC);
        boolean enoughShards = ResearchMenu.hasEnoughShards(menu.getSlot(0).getItem());
        boolean enoughMana = data.mana() >= ResearchMenu.MANA_COST;

        studyButton.active = enoughShards && enoughMana;
        if (!enoughShards) {
            studyButton.setTooltip(Tooltip.create(Component.translatable("hollowmancy.research_screen.place_dull_shards_in_the_slot", ResearchMenu.SHARD_COST)));
        } else if (!enoughMana) {
            studyButton.setTooltip(Tooltip.create(Component.translatable("hollowmancy.spells.not_enough_mana")));
        } else {
            studyButton.setTooltip(null);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        MagicData data = minecraft.player.getData(ModAttachments.MAGIC);

        // Mana gauge: fills smoothly with the swirling mana texture
        int x = leftPos + GAUGE_X, y = topPos + GAUGE_Y;
        if (shownMana < 0) shownMana = data.mana();
        shownMana = Mth.lerp(0.15F, shownMana, data.mana());
        float fraction = Mth.clamp(shownMana / Math.max(1, data.maxMana()), 0.0F, 1.0F);
        int filled = Math.round(GAUGE_W * fraction);
        graphics.fill(x - 1, y - 1, x + GAUGE_W + 1, y + GAUGE_H + 1, 0xFF0A0610);
        graphics.fill(x, y, x + GAUGE_W, y + GAUGE_H, 0xFF1A1226);
        if (filled > 0) {
            TextureAtlasSprite swirl = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SWIRL);
            graphics.enableScissor(x, y, x + filled, y + GAUGE_H);
            for (int tx = x; tx < x + GAUGE_W; tx += 16) graphics.blit(tx, y - 5, 0, 16, 16, swirl);
            graphics.disableScissor();
            graphics.fill(x + filled - 1, y, x + filled, y + GAUGE_H, 0xFFE8DEFF);
        }
        // a notch where studying's cost sits
        int notch = x + Math.round(GAUGE_W * Mth.clamp((float) ResearchMenu.MANA_COST / Math.max(1, data.maxMana()), 0.0F, 1.0F));
        boolean enoughMana = data.mana() >= ResearchMenu.MANA_COST;
        graphics.fill(notch, y - 2, notch + 1, y + GAUGE_H + 2, enoughMana ? 0xFFF0D27A : 0xFFFF6A7A);

        // One pip per shard needed, lit for each shard in the slot
        ItemStack shards = menu.getSlot(0).getItem();
        int have = shards.is(Hollowmancy.DULL_SHARD.get()) ? shards.getCount() : 0;
        for (int i = 0; i < ResearchMenu.SHARD_COST; i++) {
            int px = leftPos + PIPS_X + i * 7, py = topPos + PIPS_Y;
            boolean lit = i < have;
            graphics.fill(px + 1, py, px + 3, py + 4, lit ? 0xFFC9A0FF : 0xFF3A2A52);
            graphics.fill(px, py + 1, px + 4, py + 3, lit ? 0xFFC9A0FF : 0xFF3A2A52);
            if (lit) graphics.fill(px + 1, py + 1, px + 3, py + 3, 0xFFF4ECFF);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MagicData data = minecraft.player.getData(ModAttachments.MAGIC);
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE8D8FF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFB8A8D0, false);
        Component knowledge = Component.translatable("hollowmancy.research_screen.knowledge", data.knowledge());
        graphics.drawString(font, knowledge, imageWidth - 8 - font.width(knowledge), titleLabelY, 0xFFF0D27A, false);
        graphics.drawString(font, Component.translatable("hollowmancy.research_screen.shards_mana", ResearchMenu.SHARD_COST, ResearchMenu.MANA_COST),
                62, 21, 0xFFB8A8D0, false);
        Component mana = Component.translatable("hollowmancy.research_screen.mana_gauge", data.mana(), data.maxMana());
        graphics.pose().pushPose();
        graphics.pose().translate(GAUGE_X + GAUGE_W / 2.0F, GAUGE_Y + GAUGE_H + 2, 0);
        graphics.pose().scale(0.75F, 0.75F, 1.0F);
        graphics.drawString(font, mana, -font.width(mana) / 2, 0, 0xFF9A88C0, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
