package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.archmage.LegendarySpells;

import net.minecraft.locale.Language;

import com.mojang.blaze3d.systems.RenderSystem;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.network.LearnSpellPayload;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Spellbook: a two-page leather book. School bookmarks on top, spell list on the left, details on the right.
 * Everything is drawn from textures/gui/spellbook.png: the book at the top, decorative pieces underneath it.
 */
public class SpellbookScreen extends Screen {
    // Hollowmancy: draw the book, but without blurring or darkening the world behind it
    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
    }

    // Hollowmancy: the world keeps going while you read (the runes need it, like a chest's screen)
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/gui/spellbook.png");

    private static final int BOOK_W = 256;
    private static final int BOOK_H = 180;
    private static final int TAB_H = 19;      // how far an open bookmark rises above the book
    private static final int LEFT_X = 18;     // left page text area, relative to the book
    private static final int RIGHT_X = 137;   // right page text area
    private static final int PAGE_W = 100;
    private static final int PAGE_TOP = 13;
    private static final int ROW_TOP = 46;
    private static final int ROW_H = 22;
    private static final int BUTTON_Y = 146;

    // Pieces of the texture, below the book
    private static final int TAB_U = 0, TAB_V = 182, TAB_SW = 28, TAB_SH = 22, TAB_GAP = 3;
    private static final int SYMBOL_U = 30, SYMBOL_V = 182, SYMBOL = 11;
    private static final int FRAME_U = 0, FRAME_V = 206;
    private static final int DROP_U = 22, GLASS_U = 32, SPIRAL_U = 42, LOCK_U = 52, STAR_U = 62, ICON_V = 206;
    private static final int DIVIDER_U = 0, DIVIDER_V = 230;
    private static final int WASH_U = 0, WASH_V = 238;

    private static final int INK = 0xFF3B2A1A;
    private static final int MUTED = 0xFF7A6650;
    private static final int FAINT = 0xFFA89880;
    private static final int WARNING = 0xFF8A3A2A;

    // Remembered between openings
    private static MagicSchool currentTab = MagicSchool.GENERAL;
    private static final Map<MagicSchool, String> VIEWED = new EnumMap<>(MagicSchool.class);

    private MagicData lastData;
    private int bookX;
    private int bookY;

    public SpellbookScreen() {
        super(Component.translatable("item.hollowmancy.spellbook"));
    }

    @Override
    protected void init() {
        bookX = (width - BOOK_W) / 2;
        bookY = (height - BOOK_H) / 2 + TAB_H / 2;
        rebuild();
    }

    private MagicData data() {
        return minecraft.player.getData(ModAttachments.MAGIC);
    }

    /** The spell shown on the right page for the current school. */
    private Spell viewedSpell() {
        List<Spell> spells = Spells.inSchool(currentTab);
        if (spells.isEmpty()) return null;
        String id = VIEWED.get(currentTab);
        for (Spell spell : spells) {
            if (spell.getId().equals(id)) return spell;
        }
        return spells.get(0);
    }

    /** Spells the Spellbook can't teach, and the clue to where they're found. Null if it can be studied. */
    private static String sealedClue(Spell spell) {
        String id = spell.getId();
        if (com.walnutbeach.hollowmancy.spell.TomeSpells.TOME_ONLY.contains(id)) {
            return "This spell can't be learned by study.\nIts secret lies in the Mirror Sea, guarded in the\nAbyssal Shrine. Find the Tome of Reflection.";
        }
        if (id.startsWith("craft_")) {
            return "A spell you write yourself.\nInscribe it on a scroll at a crafting table, then read the scroll.";
        }
        return null;
    }

    /** The description without the 'Learned only from...' note (that goes in the lock's clue instead). */
    private static String pageText(Spell spell) {
        return spell.getDescription().replaceAll("\\s*Learned only[^.]*\\.", "").trim();
    }

    // ---------------- Buttons ----------------

    private void rebuild() {
        clearWidgets();
        MagicData data = data();
        lastData = data;

        if (!currentTab.isUnlocked(data)) {
            BookButton unlock = new BookButton(bookX + LEFT_X, bookY + BUTTON_Y, PAGE_W, 18,
                    Component.translatable("hollowmancy.spellbook_screen.begin_study", currentTab.getKnowledgeCost()),
                    b -> send(currentTab.unlockId()), true, false);
            unlock.active = data.stage() >= currentTab.getRequiredStage()
                    && data.knowledge() >= currentTab.getKnowledgeCost();
            addRenderableWidget(unlock);
            return;
        }

        Spell spell = viewedSpell();
        if (spell == null) return;
        int rx = bookX + RIGHT_X;

        if (!Spells.isKnown(data, spell) && sealedClue(spell) != null) return;   // sealed: a lock instead of a button

        boolean prepared = Spells.isKnown(data, spell) && ClientSpellState.isSelected(data, spell);
        BookButton main = new BookButton(rx, bookY + BUTTON_Y, 60, 18, mainLabel(spell, data), b -> onMain(spell), false, prepared);
        main.active = mainActive(spell, data);
        addRenderableWidget(main);

        if (Spells.isKnown(data, spell)) {
            int next = Spells.getTier(data, spell) + 1;
            if (next <= 4) {
                BookButton upgrade = new BookButton(rx + 62, bookY + BUTTON_Y, PAGE_W - 62, 18,
                        Component.literal(Spells.tierNumeral(next) + " (" + Spells.upgradeCost(next) + ")"),
                        b -> send(Spells.TIER_PREFIX + spell.getId()), true, false);
                upgrade.setTooltip(Tooltip.create(Component.literal(upgradeTooltip(spell, next, data))));
                upgrade.active = Spells.canUpgrade(data, spell);
                addRenderableWidget(upgrade);
            }
        }
    }

    private Component mainLabel(Spell spell, MagicData data) {
        if (Spells.isKnown(data, spell)) {
            return (ClientSpellState.isSelected(data, spell) ? Component.translatable("hollowmancy.spellbook_screen.prepared") : Component.translatable("hollowmancy.spellbook_screen.prepare"));
        }
        if (data.stage() < spell.getRequiredStage()) return Component.translatable("hollowmancy.spellbook_screen.locked");
        return Component.translatable("hollowmancy.spellbook_screen.learn", spell.getKnowledgeCost());
    }

    private boolean mainActive(Spell spell, MagicData data) {
        if (Spells.isKnown(data, spell)) return !ClientSpellState.isSelected(data, spell);
        if (data.stage() < spell.getRequiredStage()) return false;
        return data.knowledge() >= spell.getKnowledgeCost();
    }

    private void onMain(Spell spell) {
        if (Spells.isKnown(data(), spell)) {
            ClientSpellState.select(spell);
            playSound(SoundEvents.ENCHANTMENT_TABLE_USE);
            rebuild();
        } else {
            send(spell.getId());
        }
    }

    private String upgradeTooltip(Spell spell, int next, MagicData data) {
        String text = spell.getName() + " " + Spells.tierNumeral(next) + ": " + (next == 4 ? Spells.tierIvText() : spell.upgradeText(next));
        if (next < 4) {
            text += "\n" + (next == 2 ? "Also 15% cheaper and recharges 20% faster."
                    : "Also 30% cheaper and recharges 40% faster in total.");
        }
        // The six signature spells become legendary at tier IV, for an Archmage
        if (next == 4 && LegendarySpells.isSignature(spell)) {
            String id = spell.getId();
            text += "\n" + Language.getInstance().getOrDefault("hollowmancy.legendary.tooltip", "Legendary form (Archmage): %s.")
                    .replace("%s", Language.getInstance().getOrDefault("hollowmancy.legendary." + id, id))
                    + " " + Language.getInstance().getOrDefault("hollowmancy.legendary." + id + ".desc", "");
        }
        if (next == 4 && data.stage() < Spells.TIER_IV_STAGE) {
            text += "\nRequires " + MagicStage.name(Spells.TIER_IV_STAGE);
        }
        if (next == 3 && data.stage() < Spells.TIER_III_STAGE) {
            text += "\nRequires " + MagicStage.name(Spells.TIER_III_STAGE);
        }
        return text;
    }

    private void send(String id) {
        PacketDistributor.sendToServer(new LearnSpellPayload(id));
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0f));
    }

    // ---------------- Clicking tabs and spell rows ----------------

    private int tabX(int index) {
        int n = MagicSchool.values().length;
        int total = n * TAB_SW + (n - 1) * TAB_GAP;
        return bookX + (BOOK_W - total) / 2 + index * (TAB_SW + TAB_GAP);
    }

    private boolean overTab(int index, double mouseX, double mouseY) {
        int tx = tabX(index);
        return mouseX >= tx && mouseX < tx + TAB_SW && mouseY >= bookY - TAB_H && mouseY < bookY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            MagicSchool[] schools = MagicSchool.values();
            for (int i = 0; i < schools.length; i++) {
                if (overTab(i, mouseX, mouseY)) {
                    if (schools[i] != currentTab) {
                        currentTab = schools[i];
                        playSound(SoundEvents.BOOK_PAGE_TURN);
                        rebuild();
                    }
                    return true;
                }
            }

            if (currentTab.isUnlocked(data())) {
                List<Spell> spells = Spells.inSchool(currentTab);
                for (int j = 0; j < spells.size(); j++) {
                    int ry = bookY + ROW_TOP + j * ROW_H;
                    if (mouseX >= bookX + LEFT_X - 2 && mouseX < bookX + LEFT_X + PAGE_W + 2
                            && mouseY >= ry - 2 && mouseY < ry + ROW_H - 4) {
                        VIEWED.put(currentTab, spells.get(j).getId());
                        playSound(SoundEvents.BOOK_PAGE_TURN);
                        rebuild();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Close with the inventory key too, like any container
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        if (!data().equals(lastData)) rebuild();
    }

    // ---------------- Drawing ----------------

    /** School colors are bright; darken them so they read on parchment. */
    private static int ink(MagicSchool school) {
        if (school == MagicSchool.GENERAL) return 0xFF5A3C8A;
        int c = school.getColor();
        int r = (int) (((c >> 16) & 0xFF) * 0.6);
        int g = (int) (((c >> 8) & 0xFF) * 0.6);
        int b = (int) ((c & 0xFF) * 0.6);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void sprite(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        RenderSystem.enableBlend();   // text and fills switch it off; the soft pieces need it
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, x, y, u, v, w, h, 256, 256);
    }

    private void divider(GuiGraphics graphics, int x, int y) {
        sprite(graphics, x, y, DIVIDER_U, DIVIDER_V, PAGE_W, 7);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Bookmarks behind the book, then the book, then the open bookmark on top so it looks attached
        MagicSchool[] schools = MagicSchool.values();
        for (int i = 0; i < schools.length; i++) {
            if (schools[i] != currentTab) drawTab(graphics, i, schools[i], false, overTab(i, mouseX, mouseY));
        }
        // soft shadow under the book
        graphics.fill(bookX + 3, bookY + BOOK_H, bookX + BOOK_W - 1, bookY + BOOK_H + 3, 0x40000000);
        graphics.fill(bookX + BOOK_W, bookY + 4, bookX + BOOK_W + 3, bookY + BOOK_H + 3, 0x40000000);
        sprite(graphics, bookX, bookY, 0, 0, BOOK_W, BOOK_H);
        for (int i = 0; i < schools.length; i++) {
            if (schools[i] == currentTab) drawTab(graphics, i, schools[i], true, false);
        }
    }

    private void drawTab(GuiGraphics graphics, int index, MagicSchool school, boolean open, boolean hovered) {
        int x = tabX(index);
        int top = bookY - TAB_H + (open ? 0 : (hovered ? 2 : 4));
        int c = school == MagicSchool.GENERAL ? 0xB89AE0 : school.getColor();
        float shade = open ? 1.0f : (hovered ? 0.85f : 0.62f);
        graphics.setColor(((c >> 16) & 0xFF) / 255f * shade, ((c >> 8) & 0xFF) / 255f * shade, (c & 0xFF) / 255f * shade, 1.0f);
        sprite(graphics, x, top, TAB_U, TAB_V, TAB_SW, TAB_SH);
        float s = open ? 1.0f : 0.8f;
        graphics.setColor(s, s, s, 1.0f);
        int symbol = Math.min(school.ordinal(), 5);
        sprite(graphics, x + (TAB_SW - SYMBOL) / 2, top + 4, SYMBOL_U + symbol * 12, SYMBOL_V, SYMBOL, SYMBOL);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); // background, book and buttons

        MagicData data = data();
        drawLeftPage(graphics, data, mouseX, mouseY);
        drawRightPage(graphics, data, mouseX, mouseY);

        // Bookmark tooltips: the school's name and whether it's open to you
        MagicSchool[] schools = MagicSchool.values();
        for (int i = 0; i < schools.length; i++) {
            if (overTab(i, mouseX, mouseY)) {
                MagicSchool school = schools[i];
                String status = school.isUnlocked(data) ? "Open"
                        : data.stage() < school.getRequiredStage() ? "Requires " + MagicStage.name(school.getRequiredStage())
                        : "Closed: " + school.getKnowledgeCost() + " Knowledge to open";
                graphics.renderComponentTooltip(font,
                        List.of(Component.literal(school.getDisplayName()), Component.literal(status).withStyle(ChatFormatting.GRAY)),
                        mouseX, mouseY);
            }
        }
    }

    private void drawLeftPage(GuiGraphics graphics, MagicData data, int mouseX, int mouseY) {
        int lx = bookX + LEFT_X;
        int ty = bookY + PAGE_TOP;

        // Heading: the school's name, then your Knowledge, then a flourish
        graphics.drawString(font, currentTab.getDisplayName(), lx, ty, ink(currentTab), false);
        sprite(graphics, lx, bookY + 26, STAR_U, ICON_V, 9, 9);
        graphics.drawString(font, Component.translatable("hollowmancy.spellbook_screen.knowledge", data.knowledge()), lx + 12, bookY + 27, MUTED, false);
        divider(graphics, lx, bookY + 37);

        if (!currentTab.isUnlocked(data)) {
            sprite(graphics, lx + PAGE_W / 2 - 4, bookY + 52, LOCK_U, ICON_V, 9, 9);
            graphics.drawWordWrap(font, Component.translatable("hollowmancy.spellbook_screen.the_school_is_closed_to_you", currentTab.getDisplayName()),
                    lx, bookY + 68, PAGE_W, INK);
            String hint = data.stage() < currentTab.getRequiredStage()
                    ? "Its study requires " + MagicStage.name(currentTab.getRequiredStage()) + "."
                    : "Spend Knowledge to begin its study.";
            graphics.drawWordWrap(font, FormattedText.of(hint), lx, bookY + 96, PAGE_W, MUTED);
            return;
        }

        Spell viewed = viewedSpell();
        List<Spell> spells = Spells.inSchool(currentTab);
        for (int j = 0; j < spells.size(); j++) {
            Spell spell = spells.get(j);
            int ry = bookY + ROW_TOP + j * ROW_H;
            boolean known = Spells.isKnown(data, spell);
            boolean locked = data.stage() < spell.getRequiredStage();

            // An ink wash behind the chosen spell (fainter when you hover)
            boolean hovered = mouseX >= lx - 2 && mouseX < lx + PAGE_W + 2 && mouseY >= ry - 2 && mouseY < ry + ROW_H - 4;
            if (spell == viewed || hovered) {
                graphics.setColor(1.0f, 1.0f, 1.0f, spell == viewed ? 0.8f : 0.4f);
                sprite(graphics, lx - 2, ry - 1, WASH_U, WASH_V, 104, 18);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            sprite(graphics, lx - 2, ry - 2, FRAME_U, FRAME_V, 20, 20);
            float shade = known ? 1.0f : (locked ? 0.35f : 0.65f);
            graphics.setColor(shade, shade, shade, 1.0f);
            graphics.blit(spell.getIcon(), lx, ry, 0, 0, 16, 16, 16, 16);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (known) ManaHud.drawTierMarker(graphics, font, lx, ry, Spells.getTier(data, spell));

            String name = known ? Spells.displayName(spell, data) : spell.getName();
            int color = known ? ink(currentTab) : (locked ? FAINT : MUTED);
            boolean showLock = locked || (!known && sealedClue(spell) != null);
            fitted(graphics, name, lx + 24, ry + 4, PAGE_W - 24 - (showLock ? 11 : 0), color, false);
            if (showLock) sprite(graphics, lx + PAGE_W - 9, ry + 3, LOCK_U, ICON_V, 9, 9);
        }

        // Hovering a sealed spell's lock gives its clue
        for (int j = 0; j < spells.size(); j++) {
            Spell spell = spells.get(j);
            int ry = bookY + ROW_TOP + j * ROW_H;
            String clue = Spells.isKnown(data, spell) ? null : sealedClue(spell);
            if (clue != null && mouseX >= lx + PAGE_W - 10 && mouseX < lx + PAGE_W + 1 && mouseY >= ry + 2 && mouseY < ry + 13) {
                List<Component> lines = new java.util.ArrayList<>();
                for (String line : clue.split("\n")) lines.add(Component.literal(line).withStyle(ChatFormatting.AQUA));
                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }

        // Your rank, at the foot of the page
        divider(graphics, lx, bookY + 150);
        String stage = MagicStage.name(data.stage());
        graphics.drawString(font, stage, lx + (PAGE_W - font.width(stage)) / 2, bookY + 159, FAINT, false);
    }

    private void drawRightPage(GuiGraphics graphics, MagicData data, int mouseX, int mouseY) {
        int rx = bookX + RIGHT_X;
        int ty = bookY + PAGE_TOP;

        if (!currentTab.isUnlocked(data)) {
            String line = "Its secrets wait.";
            divider(graphics, rx, bookY + 70);
            graphics.drawString(font, Component.literal(line).withStyle(ChatFormatting.ITALIC),
                    rx + (PAGE_W - font.width(line)) / 2, bookY + 82, FAINT, false);
            divider(graphics, rx, bookY + 94);
            return;
        }
        Spell spell = viewedSpell();
        if (spell == null) return;

        boolean known = Spells.isKnown(data, spell);
        boolean locked = data.stage() < spell.getRequiredStage();
        int tier = known ? Spells.getTier(data, spell) : 1;

        // Large icon in a gold frame
        graphics.pose().pushPose();
        graphics.pose().translate(rx + PAGE_W / 2.0f - 20, ty - 4, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        sprite(graphics, 0, 0, FRAME_U, FRAME_V, 20, 20);
        float shade = known ? 1.0f : 0.6f;
        graphics.setColor(shade, shade, shade, 1.0f);
        graphics.blit(spell.getIcon(), 2, 2, 0, 0, 16, 16, 16, 16);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();

        String name = known ? Spells.displayName(spell, data) : spell.getName();
        fitted(graphics, name, rx, ty + 39, PAGE_W, ink(currentTab), true);
        Component school = Component.translatable("hollowmancy.spellbook_screen.magic", spell.getSchool().getDisplayName()).withStyle(ChatFormatting.ITALIC);
        graphics.drawString(font, school, rx + (PAGE_W - font.width(school)) / 2, ty + 49, MUTED, false);
        divider(graphics, rx, ty + 60);

        // Costs sit just above the buttons; the description gets whatever room is left
        int charge = Spells.effectiveCharge(tier, spell.getChargeTicks());
        int rows = 1 + (charge > 0 ? 1 : 0) + (locked ? 1 : 0);
        int statsTop = bookY + BUTTON_Y - 3 - rows * 10;

        FormattedText description = FormattedText.of(pageText(spell));
        int descTop = ty + 70;
        int room = statsTop - 3 - descTop;
        // Write it at the largest size that fits; if nothing fits, cut it off and show the rest on hover
        float scale = 1.0f;
        for (float tryScale : new float[] {1.0f, 0.9f, 0.8f, 0.7f, 0.62f}) {
            scale = tryScale;
            int lines = font.split(description, (int) (PAGE_W / tryScale)).size();
            if (lines * 9 * tryScale <= room) break;
        }
        int linesAtScale = font.split(description, (int) (PAGE_W / scale)).size();
        boolean cut = linesAtScale * 9 * scale > room;
        graphics.enableScissor(rx, descTop, rx + PAGE_W, descTop + room);
        graphics.pose().pushPose();
        graphics.pose().translate(rx, descTop, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawWordWrap(font, description, 0, 0, (int) (PAGE_W / scale), INK);
        graphics.pose().popPose();
        graphics.disableScissor();
        if (cut && mouseX >= rx && mouseX < rx + PAGE_W && mouseY >= descTop && mouseY < descTop + room) {
            graphics.renderTooltip(font, font.split(description, 180), mouseX, mouseY);
        }

        // Sealed spells: a lock where the button would be, with the clue on hover
        String clue = known ? null : sealedClue(spell);
        if (clue != null) {
            int by = bookY + BUTTON_Y;
            sprite(graphics, rx, by + 4, LOCK_U, ICON_V, 9, 9);
            graphics.drawString(font, Component.translatable("hollowmancy.spellbook_screen.sealed").withStyle(ChatFormatting.ITALIC), rx + 13, by + 5, WARNING, false);
            graphics.drawString(font, Component.literal("(?)"), rx + 50, by + 5, FAINT, false);
            if (mouseX >= rx && mouseX < rx + PAGE_W && mouseY >= by && mouseY < by + 18) {
                List<Component> lines = new java.util.ArrayList<>();
                for (String line : clue.split("\n")) lines.add(Component.literal(line).withStyle(ChatFormatting.AQUA));
                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }

        // Mana and recharge share a line; channel time and requirements go below
        int y = statsTop;
        stat(graphics, rx, y, DROP_U, Spells.effectiveManaCost(tier, spell.getManaCost()) + " mana", MUTED);
        stat(graphics, rx + 56, y, GLASS_U, seconds(Spells.effectiveCooldown(tier, spell.getCooldownTicks())), MUTED);
        if (charge > 0) {
            y += 10;
            stat(graphics, rx, y, SPIRAL_U, seconds(charge) + " to channel", MUTED);
        }
        if (locked) {
            y += 10;
            stat(graphics, rx, y, LOCK_U, "Requires " + MagicStage.name(spell.getRequiredStage()), WARNING);
        }
    }

    /** Draws text no wider than maxWidth: shrinks it a little first, then trims it with an ellipsis. */
    private void fitted(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color, boolean centered) {
        int full = font.width(text);
        float scale = Math.max(0.75f, Math.min(1.0f, maxWidth / (float) full));
        String shown = text;
        while (font.width(shown) * scale > maxWidth && shown.length() > 1) {
            shown = shown.substring(0, shown.length() - 1);
            if (font.width(shown.trim() + "\u2026") * scale <= maxWidth) { shown = shown.trim() + "\u2026"; break; }
        }
        float w = font.width(shown) * scale;
        float drawX = centered ? x + (maxWidth - w) / 2.0f : x;
        graphics.pose().pushPose();
        graphics.pose().translate(drawX, y + (1.0f - scale) * 4.0f, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, shown, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void stat(GuiGraphics graphics, int x, int y, int iconU, String text, int color) {
        sprite(graphics, x, y - 1, iconU, ICON_V, 9, 9);
        graphics.drawString(font, text, x + 12, y, color, false);
    }

    private static String seconds(int ticks) {
        double s = ticks / 20.0;
        return (s == Math.floor(s) ? String.valueOf((int) s) : String.format("%.1f", s)) + "s";
    }

    // ---------------- Book-style buttons ----------------

    /** A leather plaque with a gold rim (violet, or gold for upgrades and the prepared spell). */
    private static final class BookButton extends Button {
        private final boolean gold;
        private final boolean lit;

        BookButton(int x, int y, int w, int h, Component message, OnPress onPress, boolean gold, boolean lit) {
            super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
            this.gold = gold;
            this.lit = lit;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hover = active && isHoveredOrFocused();
            boolean useGold = lit || (gold && active);
            int u = useGold ? 170 : 104;
            int v = useGold ? (hover ? 201 : 182) : (!active ? 220 : hover ? 201 : 182);
            RenderSystem.enableBlend();
            int left = width / 2;
            graphics.blit(TEXTURE, getX(), getY(), u, v, left, 18, 256, 256);
            graphics.blit(TEXTURE, getX() + left, getY(), u + 64 - (width - left), v, width - left, 18, 256, 256);
            int color = useGold ? 0xFF3A2408 : (active ? 0xFFF4E8FF : 0xFFB8B0B8);
            Font font = Minecraft.getInstance().font;
            graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2, getY() + 5, color, false);
        }
    }
}
