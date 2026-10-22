package com.walnutbeach.hollowmancy.spell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/** Spells that can't be learned by study in the Spellbook, only from a tome found in the world. */
public final class TomeSpells {

    public static final Set<String> TOME_ONLY = Set.of("reflect");

    /** If this spell is tome-only, tell the player and return true (the Spellbook should do nothing). */
    public static boolean refuse(ServerPlayer player, String id) {
        if (id.startsWith("craft_")) {
            player.displayClientMessage(Component.translatable("hollowmancy.tome_spells.crafted_spells_are_inscribed_on_scrolls")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
            return true;
        }
        if (!TOME_ONLY.contains(id)) return false;
        player.displayClientMessage(Component.translatable("hollowmancy.tome_spells.this_spell_can_t_be_learned")
            .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
        return true;
    }

    private TomeSpells() {}
}
