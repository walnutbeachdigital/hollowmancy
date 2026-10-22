package com.walnutbeach.hollowmancy.familiar;

import net.minecraft.locale.Language;

/** Familiar text in the player's language (English if there's no translation), with %s filled in. */
public final class FamiliarText {
    public static String tr(String key, String english, Object... args) {
        String pattern = Language.getInstance().getOrDefault(key, english);
        try {
            return String.format(pattern, args);
        } catch (RuntimeException badPattern) {
            return String.format(english, args);
        }
    }

    private FamiliarText() {}
}
