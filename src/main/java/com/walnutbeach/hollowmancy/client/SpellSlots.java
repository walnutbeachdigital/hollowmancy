package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Five quick-cast spell slots. Assign them in the spell wheel (point at a spell, press 1-5);
 * cast them with Alt+1 to Alt+5. Slots are remembered on this computer, in config/hollowmancy_spell_slots.txt.
 */
public class SpellSlots {

    public static final int COUNT = 5;
    private static final String CATEGORY = "key.categories.hollowmancy";
    private static final String[] SLOTS = new String[COUNT];
    private static boolean loaded = false;

    public static final KeyMapping[] KEYS = new KeyMapping[COUNT];
    static {
        for (int i = 0; i < COUNT; i++) {
            KEYS[i] = new KeyMapping("key.hollowmancy.spell_slot_" + (i + 1), KeyConflictContext.IN_GAME, KeyModifier.ALT,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_1 + i, CATEGORY);
        }
    }

    // ---------------- Storage ----------------

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("hollowmancy_spell_slots.txt");
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        Arrays.fill(SLOTS, "");
        try {
            if (!Files.exists(file())) return;
            List<String> lines = Files.readAllLines(file());
            for (int i = 0; i < COUNT && i < lines.size(); i++) SLOTS[i] = lines.get(i).trim();
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try {
            Files.write(file(), Arrays.asList(SLOTS));
        } catch (IOException ignored) {
        }
    }

    /** The spell in a slot, if it's one this player knows. */
    public static Spell get(int slot, MagicData data) {
        load();
        Spell spell = Spells.get(SLOTS[slot]);
        return spell != null && Spells.isKnown(data, spell) ? spell : null;
    }

    /** Which slot (0-4) holds this spell, or -1. */
    public static int slotOf(Spell spell) {
        load();
        for (int i = 0; i < COUNT; i++) if (SLOTS[i].equals(spell.getId())) return i;
        return -1;
    }

    /** Put a spell in a slot; doing it again clears the slot. Returns true if it was assigned. */
    public static boolean toggle(int slot, Spell spell) {
        load();
        if (SLOTS[slot].equals(spell.getId())) {
            SLOTS[slot] = "";
            save();
            return false;
        }
        for (int i = 0; i < COUNT; i++) if (SLOTS[i].equals(spell.getId())) SLOTS[i] = "";   // one slot per spell
        SLOTS[slot] = spell.getId();
        save();
        return true;
    }

    // ---------------- Keys and casting ----------------

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Setup {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            for (KeyMapping key : KEYS) event.register(key);
        }

        @SubscribeEvent
        public static void registerHud(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "spell_slots"),
                SpellSlots::renderHud);
        }
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
    public static class Input {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            MagicData data = mc.player.getData(ModAttachments.MAGIC);
            for (int i = 0; i < COUNT; i++) {
                while (KEYS[i].consumeClick()) {
                    if (data.stage() < MagicStage.AWAKENING) continue;
                    Spell spell = get(i, data);
                    if (spell == null) {
                        mc.player.displayClientMessage(Component.translatable("hollowmancy.spell_slots.slot_is_empty_open_the_spell", (i + 1), (i + 1))
                            .withStyle(ChatFormatting.GRAY), true);
                    } else {
                        ClientInputEvents.tryCast(mc, spell);
                    }
                }
            }
        }
    }

    // ---------------- The slot bar (above the mana bar) ----------------

    private static void renderHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        MagicData data = mc.player.getData(ModAttachments.MAGIC);
        if (data.stage() < MagicStage.AWAKENING) return;

        boolean any = false;
        for (int i = 0; i < COUNT; i++) any |= get(i, data) != null;
        if (!any) return;

        // Hollowmancy HUD: framed slots just right of the hotbar, like extra hotbar slots
        int x = graphics.guiWidth() / 2 + 91 + 6;
        int y = graphics.guiHeight() - 20;
        long now = mc.level.getGameTime();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        for (int i = 0; i < COUNT; i++) {
            int sx = x + i * 19;
            graphics.blit(ManaHud.SPRITES, sx, y, 32, 16, 18, 18, 256, 64);
            Spell spell = get(i, data);
            if (spell != null) {
                graphics.blit(spell.getIcon(), sx + 1, y + 1, 0, 0, 16, 16, 16, 16);
                float cooling = ClientSpellState.cooldownFraction(spell.getId(), now);
                if (cooling > 0) {
                    int shade = Math.round(16 * cooling);
                    graphics.fill(sx + 1, y + 1, sx + 17, y + 1 + shade, 0xB0100818);
                }
            }
            graphics.pose().pushPose();
            graphics.pose().translate(sx + 2, y + 2, 200);
            graphics.pose().scale(0.75f, 0.75f, 1.0f);
            graphics.drawString(mc.font, String.valueOf(i + 1), 0, 0, 0xFFFFD75E, true);
            graphics.pose().popPose();
        }
    }
}
