package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
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
import com.walnutbeach.hollowmancy.HollowmancyConfig;

/** Found in wizard towers. Study it for Knowledge. */
public class TornPageItem extends Item {

    public static final int KNOWLEDGE_REWARD = 3;

    private static final String[] SCRAPS = {
        "...the circle must be closed before the candle is lit, never after...",
        "...mana is not spent, only moved. Remember that and you will never run dry...",
        "...the storm chose me on the third night. I was not ready. No one is...",
        "...do not trust the black stone. Whatever it touches, it keeps...",
        "...a ley line is a river. Build your house on its bank...",
        "...the frost answers faster if you do not fear it...",
    };

    public TornPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);

        MagicData data = MagicHelper.get(serverPlayer);
        if (data.stage() < MagicStage.AWAKENING) {
            player.displayClientMessage(Component.translatable("hollowmancy.torn_page_item.the_writing_swims_before_your_eyes")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return InteractionResultHolder.fail(stack);
        }

        MagicHelper.set(serverPlayer, data.withKnowledge(data.knowledge() + HollowmancyConfig.TORN_PAGE_KNOWLEDGE.get()));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        serverPlayer.sendSystemMessage(Component.literal(SCRAPS[level.random.nextInt(SCRAPS.length)])
            .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.torn_page_item.knowledge", HollowmancyConfig.TORN_PAGE_KNOWLEDGE.get()).withStyle(ChatFormatting.GRAY));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.1f);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.torn_page_item.a_wizard_s_scribbled_notes").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("hollowmancy.torn_page_item.right_click_to_study_knowledge", KNOWLEDGE_REWARD).withStyle(ChatFormatting.GRAY));
    }
}
