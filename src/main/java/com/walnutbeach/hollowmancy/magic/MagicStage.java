package com.walnutbeach.hollowmancy.magic;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public final class MagicStage {
    public static final int MUNDANE = 1;
    public static final int AWAKENING = 2;
    public static final int APPRENTICE = 3;
    public static final int ARCANE_ARTS = 4;
    public static final int MASTERY = 5;
    public static final int ASCENSION = 6;
    public static final int ARCHMAGE = 7;

    /** English names, used when a language file has no entry. Translations live in en_us.json under hollowmancy.stage.* */
    private static final String[] NAMES = {
            "The Mundane", "The Awakening", "Apprentice",
            "The Arcane Arts", "Mastery", "Ascension", "Archmage"
    };
    private static final String[] KEYS = {
            "mundane", "awakening", "apprentice", "arcane_arts", "mastery", "ascension", "archmage"
    };

    public static String key(int stage) {
        return "hollowmancy.stage." + KEYS[Mth.clamp(stage, 1, 7) - 1];
    }

    /** The stage's name in the player's language (on the client; English on a dedicated server). */
    public static String name(int stage) {
        return Language.getInstance().getOrDefault(key(stage), NAMES[Mth.clamp(stage, 1, 7) - 1]);
    }

    /** For chat messages sent from the server: each player sees it in their own language. */
    public static MutableComponent component(int stage) {
        return Component.translatableWithFallback(key(stage), NAMES[Mth.clamp(stage, 1, 7) - 1]);
    }

    private MagicStage() {}
}
