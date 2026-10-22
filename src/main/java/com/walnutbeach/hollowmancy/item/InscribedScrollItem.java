package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.CraftedSpell;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/** A scroll carrying one crafted spell. Read it (for Knowledge) to learn the spell for good. */
public class InscribedScrollItem extends Item {

    private static final String KEY = "hollowmancy_spell";

    public InscribedScrollItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(String spellId) {
        ItemStack stack = new ItemStack(SpellCraftingItems.INSCRIBED_SCROLL.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY, spellId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static CraftedSpell spellOf(ItemStack stack) {
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(KEY);
        return Spells.get(id) instanceof CraftedSpell crafted ? crafted : null;
    }

    @Override
    public Component getName(ItemStack stack) {
        CraftedSpell spell = spellOf(stack);
        return spell == null ? super.getName(stack) : Component.translatable("hollowmancy.inscribed_scroll_item.scroll_of", spell.getName());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);
        CraftedSpell spell = spellOf(stack);
        if (spell == null) return InteractionResultHolder.fail(stack);

        MagicData data = MagicHelper.get(serverPlayer);
        if (data.stage() < MagicStage.ARCANE_ARTS) {
            player.displayClientMessage(Component.translatable("hollowmancy.inscribed_scroll_item.the_runes_swim_before_your_eyes")
                .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (data.learned().contains(spell.getId())) {
            player.displayClientMessage(Component.translatable("hollowmancy.inscribed_scroll_item.you_already_know", spell.getName()).withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (data.knowledge() < spell.scrollCost()) {
            player.displayClientMessage(Component.translatable("hollowmancy.inscribed_scroll_item.you_need_knowledge_to_understand_this", spell.scrollCost())
                .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        MagicHelper.set(serverPlayer, data.withKnowledge(data.knowledge() - spell.scrollCost()).withLearned(spell.getId()));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.inscribed_scroll_item.the_runes_burn_into_your_memory", spell.getName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        if (!spell.getSchool().isUnlocked(MagicHelper.get(serverPlayer))) {
            serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.inscribed_scroll_item.you_ll_be_able_to_cast", spell.getSchool().getDisplayName()).withStyle(ChatFormatting.GRAY));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.9f);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CraftedSpell spell = spellOf(stack);
        if (spell == null) return;
        tooltip.add(Component.literal(spell.getDescription()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hollowmancy.inscribed_scroll_item.mana_s_recharge", spell.getManaCost(), String.format("%.1f", spell.getCooldownTicks() / 20.0)).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("hollowmancy.inscribed_scroll_item.right_click_to_learn_knowledge", spell.scrollCost()).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
