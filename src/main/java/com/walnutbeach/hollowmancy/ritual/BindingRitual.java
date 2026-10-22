package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.familiar.FamiliarEntity;
import com.walnutbeach.hollowmancy.familiar.FamiliarType;
import com.walnutbeach.hollowmancy.familiar.Familiars;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Calls a familiar to you. Only one at a time: binding a new one releases the old (its bond is kept). */
public class BindingRitual extends Ritual {

    private final FamiliarType type;

    public BindingRitual(FamiliarType type, String offeringName) {
        super("bind_" + type.id(), "Binding: " + type.displayName(),
            "Offer " + offeringName + ". Four lit candles 2 out on each side, four Amethyst Blocks 2 out on each diagonal. "
                + "Calls a " + type.displayName() + " familiar to you.",
            MagicStage.APPRENTICE, type.offering(), 50);
        this.type = type;
        requireAll(RitualParts.cardinals(2), RitualParts.LIT_CANDLE);
        requireAll(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK);
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        boolean replacing = Familiars.activeType(player) != null && Familiars.activeType(player) != type;
        FamiliarEntity familiar = Familiars.summon(player, type, Vec3.atCenterOf(altar).add(0, 1.0, 0));
        if (familiar == null) return;

        level.playSound(null, altar, SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.NEUTRAL, 1.0f, 0.9f);
        String returning = familiar.getBond() > 0 ? " It remembers you." : " It is yours.";
        player.sendSystemMessage(Component.translatable("hollowmancy.binding_ritual.a_answers_the_call", type.displayName().toLowerCase(), returning, (replacing ? " (Your other familiar returns to the wild. It will remember you too.)" : ""))
            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(familiar.statusLine()).withStyle(ChatFormatting.GRAY));
    }
}
