package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSpellState {

    private static String selectedId = "spark";

    private static final Map<String, Long> COOLDOWN_END = new HashMap<>();
    private static final Map<String, Integer> COOLDOWN_TOTAL = new HashMap<>();

    private static String channelId = null;
    private static long channelStart = 0;
    private static int channelTotal = 1;

    // ---------------- Selection ----------------

    public static Spell getSelected(MagicData data) {
        List<Spell> known = Spells.known(data);
        if (known.isEmpty()) return null;
        for (Spell spell : known) {
            if (spell.getId().equals(selectedId)) return spell;
        }
        return known.get(0);
    }

    public static boolean isSelected(MagicData data, Spell spell) {
        Spell selected = getSelected(data);
        return selected != null && selected.getId().equals(spell.getId());
    }

    public static void select(Spell spell) {
        selectedId = spell.getId();
    }

    public static void cycle(MagicData data) {
        List<Spell> known = Spells.known(data);
        if (known.isEmpty()) return;
        int index = known.indexOf(getSelected(data));
        selectedId = known.get((index + 1) % known.size()).getId();
    }

    // ---------------- Cooldowns ----------------

    public static void setCooldown(String spellId, int remaining, int total, long now) {
        COOLDOWN_END.put(spellId, now + remaining);
        COOLDOWN_TOTAL.put(spellId, Math.max(1, total));
    }

    public static int cooldownRemaining(String spellId, long now) {
        Long end = COOLDOWN_END.get(spellId);
        if (end == null) return 0;
        long left = end - now;
        int total = COOLDOWN_TOTAL.getOrDefault(spellId, 1);
        if (left <= 0 || left > total) {
            COOLDOWN_END.remove(spellId);
            return 0;
        }
        return (int) left;
    }

    public static float cooldownFraction(String spellId, long now) {
        int remaining = cooldownRemaining(spellId, now);
        if (remaining <= 0) return 0.0f;
        return (float) remaining / COOLDOWN_TOTAL.getOrDefault(spellId, 1);
    }

    // ---------------- Channeling ----------------

    public static void setChannel(String spellId, int ticks, long now) {
        if (spellId == null || spellId.isEmpty() || ticks <= 0) {
            channelId = null;
            return;
        }
        channelId = spellId;
        channelStart = now;
        channelTotal = ticks;
    }

    public static boolean isChanneling() {
        return channelId != null;
    }

    public static Spell getChannelSpell() {
        return channelId == null ? null : Spells.get(channelId);
    }

    /** 0.0 = just started, 1.0 = about to release. */
    public static float channelProgress(long now, float partialTick) {
        if (channelId == null) return 0.0f;
        return Mth.clamp((now - channelStart + partialTick) / channelTotal, 0.0f, 1.0f);
    }

    // ---------------- Reset ----------------

    public static void clearCooldowns() {
        COOLDOWN_END.clear();
        COOLDOWN_TOTAL.clear();
        channelId = null;
    }
}