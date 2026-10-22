package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SoarSpell extends Spell {

    public SoarSpell() {
        super("soar", "Soar", "Wind gathers beneath you: fly freely, draining 3 mana each second you are airborne. Cast again to land.",
                MagicSchool.ELEMENTAL, MagicStage.MASTERY, 6, 20, 40);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Drains only 2 mana per second in flight."
                : "Drains only 1 mana per second, and you fly faster.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            player.displayClientMessage(Component.translatable("hollowmancy.soar_spell.you_already_fly").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        // Already soaring: fold your wings. Returning false means this costs nothing and starts no cooldown.
        if (Soaring.isSoaring(player)) {
            Soaring.stop(player, "You fold your wings.");
            return false;
        }

        Soaring.start(player, tier(player));
        return true;
    }
}