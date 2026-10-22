package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * The magic HUD, one compact unit above the hotbar: the selected spell in a gold slot (hotbar-sized; its cooldown
 * sweeps over it and its name fades in when you switch), with the mana gauge beside it (swirling mana, the
 * amount on a plaque) and, when you're corrupted, a crimson bar beneath. Drawn from textures/gui/arcane_hud.png.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ManaHud {

    public static final ResourceLocation SPRITES = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/gui/arcane_hud.png");
    private static final int ICON = 16;

    // One unit: a 20x20 spell slot (same size as a hotbar slot), then the 122 x 9 gauge beside it
    private static final int SLOT = 20, GAP = 2;
    private static final int GAUGE_W = 122, GAUGE_H = 9;
    private static final int LEFT_X0 = 2, LEFT_X1 = 36, RIGHT_X0 = 86, RIGHT_X1 = 120, CH_Y0 = 2, CH_Y1 = 7;
    private static final int PLAQUE_X = 36, PLAQUE_W = 50;
    private static final int UNIT_W = SLOT + GAP + GAUGE_W;
    /** Where the gauge's top edge was last drawn, so the item name and action bar can make room. */
    static int gaugeTop = Integer.MAX_VALUE;

    private static float shownFraction = -1;
    private static long lastFrame = 0;
    private static String lastSpell = "";
    private static long spellChangedAt = -1000;

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "mana_bar"),
                ManaHud::render);
    }

    /** Small gold II / III in the bottom-right corner of a 16x16 icon. */
    public static void drawTierMarker(GuiGraphics graphics, Font font, int iconX, int iconY, int tier) {
        if (tier < 2) return;
        String numeral = Spells.tierNumeral(tier);
        graphics.drawString(font, numeral, iconX + ICON - font.width(numeral) + 1, iconY + ICON - 7, 0xFFFFD75E, true);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) return;

        MagicData data = player.getData(ModAttachments.MAGIC);
        if (data.stage() < MagicStage.AWAKENING || data.maxMana() <= 0) return;

        boolean corrupted = data.corruption() > 0;
        int ux = graphics.guiWidth() / 2 - UNIT_W / 2;
        int uy = rowAboveVanillaHud(mc, player, graphics.guiHeight()) - SLOT;
        gaugeTop = uy;
        int gx = ux + SLOT + GAP;
        // The gauge sits centred beside the spell; with corruption it moves up and the crimson bar goes beneath
        int gy = corrupted ? uy + 3 : uy + (SLOT - GAUGE_H) / 2;
        RenderSystem.enableBlend();

        // ---- The gauge ----
        graphics.blit(SPRITES, gx, gy, 0, 0, GAUGE_W, GAUGE_H, 256, 64);
        graphics.blit(SPRITES, ux, uy, 0, 16, SLOT, SLOT, 256, 64);

        // Ease toward the real amount so the gauge drains and fills smoothly
        float target = (float) data.mana() / data.maxMana();
        long nowMs = Util.getMillis();
        float dt = lastFrame == 0 ? 1.0f : Math.min(0.1f, (nowMs - lastFrame) / 1000.0f);
        lastFrame = nowMs;
        if (shownFraction < 0) shownFraction = target;
        shownFraction += (target - shownFraction) * Math.min(1.0f, dt * 10.0f);
        if (Math.abs(target - shownFraction) < 0.002f) shownFraction = target;

        // Mana fills the left channel first, then the right, across the plaque
        int total = (LEFT_X1 - LEFT_X0) + (RIGHT_X1 - RIGHT_X0);
        int filled = Math.round(shownFraction * total);
        TextureAtlasSprite swirl = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ManaBatteryRenderer.SWIRL);
        int leftFill = Math.min(filled, LEFT_X1 - LEFT_X0);
        int rightFill = Math.max(0, filled - (LEFT_X1 - LEFT_X0));
        channel(graphics, swirl, gx + LEFT_X0, gy, leftFill, LEFT_X1 - LEFT_X0);
        channel(graphics, swirl, gx + RIGHT_X0, gy, rightFill, RIGHT_X1 - RIGHT_X0);

        // Corruption: its own crimson bar just under the gauge, pulsing gently
        if (corrupted) {
            int cx = gx, cy = gy + GAUGE_H + 2, cw = GAUGE_W;
            graphics.blit(SPRITES, cx, cy, 0, 44, cw, 4, 256, 64);
            int corrupt = Math.round((float) data.corruption() / MagicData.MAX_CORRUPTION * (cw - 2));
            float pulse = 0.75f + 0.25f * (float) Math.sin(nowMs / 300.0);
            int red = (int) (0xE0 * pulse) + 0x1F, green = (int) (0x30 * pulse);
            graphics.fill(cx + 1, cy + 1, cx + 1 + corrupt, cy + 3, 0xFF000000 | (red << 16) | (green << 8) | 0x48);
            graphics.fill(cx + 1, cy + 1, cx + 1 + corrupt, cy + 2, 0x50FFFFFF & 0x50FF8090);
        }

        // The amount, small, on the plaque
        String amount = String.format("%,d / %,d", data.mana(), data.maxMana());
        float scale = Math.min(0.6f, (PLAQUE_W - 6) / (float) mc.font.width(amount));
        graphics.pose().pushPose();
        graphics.pose().translate(gx + PLAQUE_X + PLAQUE_W / 2.0f, gy + GAUGE_H / 2.0f - 3.5f * scale + 0.5f, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, amount, -mc.font.width(amount) / 2, 0, 0xFFF2E6FF, true);
        graphics.pose().popPose();

        // ---- The selected spell, in the slot at the left of the unit ----
        Spell selected = ClientSpellState.getSelected(data);
        if (selected == null) return;
        long now = mc.level.getGameTime();
        int iconX = ux + 2, iconY = uy + 2;
        graphics.blit(selected.getIcon(), iconX, iconY, 0, 0, ICON, ICON, ICON, ICON);

        int remaining = ClientSpellState.cooldownRemaining(selected.getId(), now);
        if (remaining > 0) {
            float fraction = ClientSpellState.cooldownFraction(selected.getId(), now);
            int overlay = Math.round(ICON * fraction);
            graphics.fill(iconX, iconY, iconX + ICON, iconY + overlay, 0xB0100818);
            String seconds = remaining >= 200 ? String.valueOf(remaining / 20) : String.format("%.1f", remaining / 20.0);
            graphics.drawCenteredString(mc.font, seconds, iconX + ICON / 2, iconY + 4, 0xFFFFFFFF);
        } else {
            drawTierMarker(graphics, mc.font, iconX, iconY, Spells.getTier(data, selected));
        }

        // Its name fades in above the gauge for a moment when you switch spells
        if (!selected.getId().equals(lastSpell)) {
            lastSpell = selected.getId();
            spellChangedAt = now;
        }
        long shown = now - spellChangedAt;
        if (shown < 50) {
            int alpha = shown < 40 ? 255 : (int) (255 * (50 - shown) / 10.0f);
            if (alpha > 8) {
                int color = (alpha << 24) | (selected.getSchool().getColor() & 0xFFFFFF);
                String name = Spells.displayName(selected, data);
                graphics.drawString(mc.font, name, graphics.guiWidth() / 2 - mc.font.width(name) / 2, uy - 10, color, true);
            }
        }
    }

    /**
     * The y of the first free row above Minecraft's own HUD: just above the hotbar in creative, above the hearts
     * in survival (and above the armour / air row when that's showing). Leaves room for the corruption bar.
     */
    private static int rowAboveVanillaHud(Minecraft mc, Player player, int screenHeight) {
        int bottom;
        if (mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
            bottom = screenHeight - 25;                                     // just above the hotbar
        } else {
            float health = player.getMaxHealth() + player.getAbsorptionAmount();
            int rows = (int) Math.ceil(health / 2.0f / 10.0f);
            int rowHeight = Math.max(10 - (rows - 2), 3);
            int heartsTop = screenHeight - 39 - (rows - 1) * rowHeight;
            boolean armourOrAir = player.getArmorValue() > 0 || player.getAirSupply() < player.getMaxAirSupply();
            bottom = heartsTop - 2 - (armourOrAir ? 10 : 0);
        }
        return bottom;
    }

    /** One channel of the gauge: the animated mana, clipped to how full it is. */
    private static void channel(GuiGraphics graphics, TextureAtlasSprite swirl, int x, int gy, int fill, int width) {
        if (fill <= 0) return;
        graphics.enableScissor(x, gy + CH_Y0, x + fill, gy + CH_Y1);
        for (int tx = x; tx < x + width; tx += 16) graphics.blit(tx, gy + CH_Y0 - 5, 0, 16, 16, swirl);
        graphics.disableScissor();
        graphics.fill(x, gy + CH_Y0, x + fill, gy + CH_Y0 + 1, 0x40FFFFFF);                       // sheen
        if (fill < width) graphics.fill(x + fill - 1, gy + CH_Y0, x + fill, gy + CH_Y1, 0xFFE8DEFF); // bright leading edge
    }

    /** Lifts the item name and action-bar text just enough to sit above the gauge instead of on it. */
    @EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
    public static class Layout {
        private static int lift = 0;

        private static boolean lifted(RenderGuiLayerEvent event) {
            return event.getName().equals(VanillaGuiLayers.SELECTED_ITEM_NAME) || event.getName().equals(VanillaGuiLayers.OVERLAY_MESSAGE);
        }

        private static boolean active() {
            Minecraft mc = Minecraft.getInstance();
            return mc.player != null && mc.player.getData(ModAttachments.MAGIC).stage() >= MagicStage.AWAKENING;
        }

        @SubscribeEvent
        public static void before(RenderGuiLayerEvent.Pre event) {
            lift = 0;
            if (!lifted(event) || !active() || gaugeTop == Integer.MAX_VALUE) return;
            Minecraft mc = Minecraft.getInstance();
            int h = event.getGuiGraphics().guiHeight();
            int textBottom = event.getName().equals(VanillaGuiLayers.OVERLAY_MESSAGE) ? h - 60
                    : h - (mc.gameMode != null && mc.gameMode.canHurtPlayer() ? 59 : 45) + 9;
            lift = Math.max(0, textBottom - (gaugeTop - 11));   // also clear of the spell name above the gauge
            if (lift > 0) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, -lift, 0);
            }
        }

        @SubscribeEvent
        public static void after(RenderGuiLayerEvent.Post event) {
            if (lifted(event) && lift > 0) {
                event.getGuiGraphics().pose().popPose();
                lift = 0;
            }
        }
    }
}
