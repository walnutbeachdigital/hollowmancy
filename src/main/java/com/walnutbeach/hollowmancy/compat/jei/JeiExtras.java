package com.walnutbeach.hollowmancy.compat.jei;

import java.util.Map;

/** Ritual descriptions for the Rituals page, beyond the ones RitualDisplay already knows. */
public final class JeiExtras {

    private static final Map<String, String> MORE = Map.ofEntries(
        Map.entry("resonance", "Only in the Crystal Barrens. Wakes the Amethyst Colossus."),
        Map.entry("bind_owl", "Calls an Owl familiar: Night Vision, spots monsters, catches your falls."),
        Map.entry("bind_wisp", "Calls a Wisp familiar: refills your mana and, in time, washes away corruption."),
        Map.entry("bind_golem", "Calls a Golem familiar: fights beside you, and shields you while it's near."),
        Map.entry("unmaking", "Only in the Hollow. Calls the Listener, the source of the Hollow. The last ritual.")
    );

    public static String effectOf(String ritualId, Map<String, String> known) {
        if (known.containsKey(ritualId)) return known.get(ritualId);
        return MORE.getOrDefault(ritualId, "See the Arcane Journal.");
    }

    private JeiExtras() {}
}
