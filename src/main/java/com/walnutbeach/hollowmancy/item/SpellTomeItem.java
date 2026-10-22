package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Read it to learn one spell. */
public class SpellTomeItem extends Item {

    private final String spellId;

    public SpellTomeItem(Properties properties, String spellId) {
        super(properties);
        this.spellId = spellId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);
        Spell spell = Spells.get(spellId);
        if (spell == null) return InteractionResultHolder.fail(stack);

        MagicData data = MagicHelper.get(serverPlayer);
        if (data.stage() < MagicStage.AWAKENING) {
            player.displayClientMessage(Component.translatable("hollowmancy.spell_tome_item.the_pages_are_blank_to_you").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (data.learned().contains(spellId)) {
            player.displayClientMessage(Component.translatable("hollowmancy.inscribed_scroll_item.you_already_know", spell.getName()).withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        MagicHelper.set(serverPlayer, data.withLearned(spellId));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.spell_tome_item.the_tome_turns_to_light_in", spell.getName())
            .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        MagicData after = MagicHelper.get(serverPlayer);
        if (!Spells.isKnown(after, spell)) {
            String need = !spell.getSchool().isUnlocked(after) ? "open the school of " + spell.getSchool().getDisplayName()
                : "reach " + MagicStage.name(spell.getRequiredStage());
            serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.spell_tome_item.you_ll_be_able_to_cast", need)
                .withStyle(ChatFormatting.GRAY));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Spell spell = Spells.get(spellId);
        if (spell != null) {
            tooltip.add(Component.translatable("hollowmancy.spell_tome_item.teaches", spell.getName(), spell.getSchool().getDisplayName())
                .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("hollowmancy.lost_page_item.right_click_to_read").withStyle(ChatFormatting.GRAY));
    }
}
