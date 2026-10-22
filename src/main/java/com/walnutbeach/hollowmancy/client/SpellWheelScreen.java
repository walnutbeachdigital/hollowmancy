package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hold G: an inner ring of schools and an outer ring of the pointed-at school's spells.
 * Point at a school, then out at a spell, and let go. Press 1-5 over a spell to put it in a quick-cast slot.
 */
public class SpellWheelScreen extends Screen {

    private static final int TAP_TICKS = 5;
    private static final int DEAD_ZONE = 14;
    private static final float GAP = 0.03f;

    private final MagicData data;
    private final List<MagicSchool> schools = new ArrayList<>();
    private final Map<MagicSchool, List<Spell>> bySchool = new LinkedHashMap<>();

    private int focusedSchool = 0;      // whose spells fill the outer ring
    private int hoveredSchool = -1;     // the cursor is on the inner ring
    private int hoveredSpell = -1;      // the cursor is on the outer ring
    private int openTicks = 0;
    private boolean tapped = false;
    private double lastMouseX = -1, lastMouseY = -1;
    private Component flash = null;
    private int flashTicks = 0;

    public SpellWheelScreen(MagicData data) {
        super(Component.translatable("key.hollowmancy.cycle_spell"));
        this.data = data;
        for (Spell spell : Spells.known(data)) bySchool.computeIfAbsent(spell.getSchool(), s -> new ArrayList<>()).add(spell);
        for (MagicSchool school : MagicSchool.values()) if (bySchool.containsKey(school)) schools.add(school);

        Spell current = ClientSpellState.getSelected(data);
        if (current != null && schools.contains(current.getSchool())) {
            focusedSchool = schools.indexOf(current.getSchool());
            hoveredSpell = spells().indexOf(current);
        }
    }

    private List<Spell> spells() {
        return schools.isEmpty() ? List.of() : bySchool.get(schools.get(focusedSchool));
    }

    // ---------------- Layout ----------------

    private float outerOut() { return Math.min(84.0f, height / 2.0f - 34.0f); }
    private float outerIn() { return outerOut() * 0.62f; }
    private float innerOut() { return outerIn() - 6.0f; }
    private float innerIn() { return innerOut() * 0.45f; }

    private static float step(int count) { return Mth.TWO_PI / Math.max(1, count); }
    private static float centerAngle(int i, int count) { return -Mth.HALF_PI + i * step(count); }

    private static int sectorAt(double dx, double dy, int count) {
        double angle = Math.atan2(dy, dx) + Mth.HALF_PI + step(count) / 2.0;
        angle = ((angle % Mth.TWO_PI) + Mth.TWO_PI) % Mth.TWO_PI;
        return Mth.clamp((int) (angle / step(count)), 0, count - 1);
    }

    private void updateHover(double mouseX, double mouseY) {
        if (schools.isEmpty()) return;
        double dx = mouseX - width / 2.0, dy = mouseY - height / 2.0;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < DEAD_ZONE) return;
        if (dist < (innerOut() + outerIn()) / 2) {
            int school = sectorAt(dx, dy, schools.size());
            hoveredSchool = school;
            if (school != focusedSchool) {
                focusedSchool = school;
                hoveredSpell = -1;
            }
        } else {
            hoveredSchool = -1;
            hoveredSpell = sectorAt(dx, dy, spells().size());
        }
    }

    // ---------------- Drawing ----------------

    private static final ResourceLocation[] GLYPHS = new ResourceLocation[8];
    static {
        for (int i = 0; i < GLYPHS.length; i++)
            GLYPHS[i] = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/particle/glyph_" + i + ".png");
    }
    /** How long the wheel takes to open, in ticks. */
    private static final float OPEN_TICKS = 4.0f;

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float open = Mth.clamp((openTicks + partialTick) / OPEN_TICKS, 0.0f, 1.0f);
        graphics.fillGradient(0, 0, width, height, (int) (0x60 * open) << 24 | 0x0A0614, (int) (0x90 * open) << 24 | 0x05030A);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            updateHover(mouseX, mouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        float cx = width / 2.0f, cy = height / 2.0f;
        if (schools.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("hollowmancy.spell_wheel_screen.you_know_no_spells_yet"), (int) cx, (int) cy - 4, 0xAAAAAA);
            return;
        }

        // Opens with a quick ease-out grow
        float open = Mth.clamp((openTicks + partialTick) / OPEN_TICKS, 0.0f, 1.0f);
        float grow = 0.82f + 0.18f * (1.0f - (1.0f - open) * (1.0f - open));
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(grow, grow, 1.0f);
        graphics.pose().translate(-cx, -cy, 0);

        List<Spell> spells = spells();
        Spell current = ClientSpellState.getSelected(data);
        int schoolCount = schools.size(), spellCount = spells.size();
        int mana = minecraft.player.getData(ModAttachments.MAGIC).mana();
        float time = openTicks + partialTick;

        RenderSystem.disableCull();
        // inner ring: schools, the focused one bright with a coloured rim
        for (int i = 0; i < schoolCount; i++) {
            int color = schools.get(i).getColor() & 0xFFFFFF;
            float a0 = centerAngle(i, schoolCount) - step(schoolCount) / 2 + GAP, a1 = centerAngle(i, schoolCount) + step(schoolCount) / 2 - GAP;
            boolean focused = i == focusedSchool;
            wedge(graphics, cx, cy, innerIn(), innerOut(), a0, a1, focused ? 0xC0000000 | scale(color, 0.75f) : 0xA0000000 | scale(color, 0.30f));
            wedge(graphics, cx, cy, innerOut() - 1.5f, innerOut(), a0, a1, focused ? 0xFF000000 | color : 0x80000000 | scale(color, 0.6f));
        }
        // outer ring: the focused school's spells
        int schoolColor = schools.get(focusedSchool).getColor() & 0xFFFFFF;
        float out = outerOut(), in = outerIn();
        for (int i = 0; i < spellCount; i++) {
            float a0 = centerAngle(i, spellCount) - step(spellCount) / 2 + GAP, a1 = centerAngle(i, spellCount) + step(spellCount) / 2 - GAP;
            boolean hovered = i == hoveredSpell;
            float o = hovered ? out + 5.0f : out;
            wedge(graphics, cx, cy, in, o, a0, a1, hovered ? 0xD0000000 | scale(schoolColor, 0.55f) : 0xC0120E1C);
            wedge(graphics, cx, cy, o - 2.0f, o, a0, a1, (hovered ? 0xFF000000 : 0xB0000000) | schoolColor);
            if (spells.get(i) == current) wedge(graphics, cx, cy, in, in + 2.0f, a0, a1, 0xFFFFFFFF);
        }
        // the hub, with a faint rim
        wedge(graphics, cx, cy, 0, innerIn() - 3.0f, 0, Mth.TWO_PI, 0xD00C0914);
        wedge(graphics, cx, cy, innerIn() - 4.0f, innerIn() - 3.0f, 0, Mth.TWO_PI, 0x90000000 | schoolColor);

        // cooldown rings around each spell: the bright part is how far it has recharged
        long now = minecraft.level.getGameTime();
        float outerMid = (in + out) / 2;
        for (int i = 0; i < spellCount; i++) {
            float cooling = ClientSpellState.cooldownFraction(spells.get(i).getId(), now);
            if (cooling <= 0) continue;
            float angle = centerAngle(i, spellCount);
            float sx = cx + Mth.cos(angle) * outerMid, sy = cy + Mth.sin(angle) * outerMid;
            float start = -Mth.HALF_PI, sweep = Mth.TWO_PI * (1.0f - cooling);
            wedge(graphics, sx, sy, 11.0f, 13.0f, 0, Mth.TWO_PI, 0xA0000000);
            if (sweep > 0.01f) wedge(graphics, sx, sy, 11.0f, 13.0f, start, start + sweep, 0xFF000000 | schoolColor);
        }
        graphics.flush();
        RenderSystem.enableCull();

        // runes turning slowly around the hub
        float runeRadius = innerIn() - 10.0f;
        if (runeRadius > 8) {
            RenderSystem.enableBlend();
            float r = ((schoolColor >> 16) & 0xFF) / 255f, g = ((schoolColor >> 8) & 0xFF) / 255f, b = (schoolColor & 0xFF) / 255f;
            for (int k = 0; k < 8; k++) {
                float a = time * 0.02f + k * Mth.TWO_PI / 8;
                int gx = Math.round(cx + Mth.cos(a) * runeRadius) - 3, gy = Math.round(cy + Mth.sin(a) * runeRadius) - 3;
                RenderSystem.setShaderColor(r, g, b, 0.35f + 0.25f * Mth.sin(time * 0.1f + k));
                graphics.blit(GLYPHS[k], gx, gy, 0, 0, 7, 7, 8, 8);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        // school icons (each school shows its first spell's icon)
        float innerMid = (innerIn() + innerOut()) / 2;
        for (int i = 0; i < schoolCount; i++) {
            Spell first = bySchool.get(schools.get(i)).get(0);
            float angle = centerAngle(i, schoolCount);
            int x = Math.round(cx + Mth.cos(angle) * innerMid) - 8, y = Math.round(cy + Mth.sin(angle) * innerMid) - 8;
            if (i != focusedSchool) RenderSystem.setShaderColor(0.6f, 0.6f, 0.65f, 1.0f);
            graphics.blit(first.getIcon(), x, y, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        // spell icons in HUD-style frames, with tier, slot number, and dimmed when you can't afford them
        for (int i = 0; i < spellCount; i++) {
            Spell spell = spells.get(i);
            float angle = centerAngle(i, spellCount);
            int x = Math.round(cx + Mth.cos(angle) * outerMid) - 8, y = Math.round(cy + Mth.sin(angle) * outerMid) - 8;
            boolean affordable = mana >= Spells.effectiveManaCost(Spells.getTier(data, spell), spell.getManaCost());
            float cooling = ClientSpellState.cooldownFraction(spell.getId(), now);
            graphics.pose().pushPose();
            if (i == hoveredSpell) {
                graphics.pose().translate(x + 8, y + 8, 0);
                graphics.pose().scale(1.3f, 1.3f, 1.0f);
                graphics.pose().translate(-(x + 8), -(y + 8), 0);
            }
            graphics.blit(ManaHud.SPRITES, x - 1, y - 1, 32, 16, 18, 18, 256, 64);
            if (!affordable) RenderSystem.setShaderColor(0.75f, 0.45f, 0.5f, 1.0f);
            else if (cooling > 0) RenderSystem.setShaderColor(0.55f, 0.55f, 0.6f, 1.0f);
            graphics.blit(spell.getIcon(), x, y, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            if (cooling > 0) {
                int secs = (int) Math.ceil(ClientSpellState.cooldownRemaining(spell.getId(), now) / 20.0);
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200);
                graphics.drawCenteredString(font, String.valueOf(secs), x + 8, y + 4, 0xFFFFFFFF);
                graphics.pose().popPose();
            }
            ManaHud.drawTierMarker(graphics, font, x, y, Spells.getTier(data, spell));
            int slot = SpellSlots.slotOf(spell);
            if (slot >= 0) graphics.drawString(font, String.valueOf(slot + 1), x - 2, y - 2, 0xFFFFD75E, true);
            graphics.pose().popPose();
        }

        // the school's name in the middle
        String schoolName = schools.get(focusedSchool).getDisplayName();
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy - 3, 0);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);
        graphics.drawCenteredString(font, schoolName, 0, 0, 0xFF000000 | schoolColor);
        graphics.pose().popPose();

        // details above the wheel
        int top = (int) (cy - out - 26);
        if (hoveredSpell >= 0 && hoveredSpell < spellCount) {
            Spell spell = spells.get(hoveredSpell);
            graphics.drawCenteredString(font, Spells.displayName(spell, data), (int) cx, top, 0xFF000000 | schoolColor);
            int remaining = ClientSpellState.cooldownRemaining(spell.getId(), now);
            int cost = Spells.effectiveManaCost(Spells.getTier(data, spell), spell.getManaCost());
            Component status = remaining > 0
                    ? Component.translatable("hollowmancy.spell_wheel_screen.recharging", String.format("%.1f", remaining / 20.0))
                    : mana < cost ? Component.translatable("hollowmancy.spell_wheel_screen.not_enough_mana", cost)
                    : Component.translatable("hollowmancy.spell_wheel_screen.mana_cost", cost);
            graphics.drawCenteredString(font, status, (int) cx, top + 11, remaining > 0 ? 0xFF9A9A : mana < cost ? 0xFF6A7A : 0x7FB8FF);
        } else {
            graphics.drawCenteredString(font, schoolName, (int) cx, top, 0xFF000000 | schoolColor);
            graphics.drawCenteredString(font, Component.translatable(spellCount == 1 ? "hollowmancy.spell_wheel_screen.one_spell" : "hollowmancy.spell_wheel_screen.spells", spellCount),
                (int) cx, top + 11, 0x9A9AAA);
        }
        graphics.pose().popPose();

        if (flash != null && flashTicks > 0) graphics.drawCenteredString(font, flash, (int) cx, height - 30, 0xFFFFD75E);
        graphics.drawCenteredString(font, Component.translatable(tapped ? "hollowmancy.spell_wheel_screen.hint_tapped" : "hollowmancy.spell_wheel_screen.hint_held"),
                (int) cx, height - 16, 0x808080);
    }

    private static int scale(int rgb, float f) {
        int r = (int) (((rgb >> 16) & 0xFF) * f), g = (int) (((rgb >> 8) & 0xFF) * f), b = (int) ((rgb & 0xFF) * f);
        return (r << 16) | (g << 8) | b;
    }

    private static void wedge(GuiGraphics graphics, float cx, float cy, float r0, float r1, float a0, float a1, int argb) {
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        int segments = Math.max(2, (int) Math.ceil((a1 - a0) / 0.08f));
        for (int s = 0; s < segments; s++) {
            float t0 = a0 + (a1 - a0) * s / segments, t1 = a0 + (a1 - a0) * (s + 1) / segments;
            float c0 = Mth.cos(t0), s0 = Mth.sin(t0), c1 = Mth.cos(t1), s1 = Mth.sin(t1);
            buffer.addVertex(matrix, cx + c0 * r0, cy + s0 * r0, 0).setColor(argb);
            buffer.addVertex(matrix, cx + c0 * r1, cy + s0 * r1, 0).setColor(argb);
            buffer.addVertex(matrix, cx + c1 * r1, cy + s1 * r1, 0).setColor(argb);
            buffer.addVertex(matrix, cx + c1 * r0, cy + s1 * r0, 0).setColor(argb);
        }
    }

    // ---------------- Input ----------------

    @Override
    public void tick() {
        openTicks++;
        if (flashTicks > 0) flashTicks--;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModKeybinds.CYCLE_SPELL.matches(keyCode, scanCode)) {
            if (openTicks > TAP_TICKS) choose();
            else tapped = true;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeybinds.CYCLE_SPELL.matches(keyCode, scanCode)) {
            if (tapped) choose();
            return true;
        }
        // 1-5 over a spell: put it in that quick-cast slot
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode < GLFW.GLFW_KEY_1 + SpellSlots.COUNT) {
            List<Spell> spells = spells();
            if (hoveredSpell >= 0 && hoveredSpell < spells.size()) {
                int slot = keyCode - GLFW.GLFW_KEY_1;
                Spell spell = spells.get(hoveredSpell);
                boolean assigned = SpellSlots.toggle(slot, spell);
                flash = (assigned ? Component.translatable("hollowmancy.spell_wheel_screen.is_in_slot_alt", spell.getName(), (slot + 1), (slot + 1)) : Component.translatable("hollowmancy.spell_wheel_screen.slot_cleared", (slot + 1)));
                flashTicks = 40;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, assigned ? 1.6f : 0.8f));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            updateHover(mouseX, mouseY);
            if (hoveredSpell >= 0) choose();
            return true;
        }
        if (button == 1) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Scroll: step through the spells; with Shift, through the schools
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (schools.isEmpty()) return false;
        int dir = scrollY > 0 ? -1 : 1;
        if (hasShiftDown()) {
            focusedSchool = Math.floorMod(focusedSchool + dir, schools.size());
            hoveredSpell = 0;
        } else {
            hoveredSpell = Math.floorMod((hoveredSpell < 0 ? 0 : hoveredSpell) + dir, spells().size());
        }
        return true;
    }

    private void choose() {
        List<Spell> spells = spells();
        if (hoveredSpell >= 0 && hoveredSpell < spells.size()) {
            Spell spell = spells.get(hoveredSpell);
            ClientSpellState.select(spell);
            minecraft.player.displayClientMessage(Component.literal(spell.getName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f));
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

