package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
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

public class LostPageItem extends Item {

    public static final int KNOWLEDGE_REWARD = 5;
    private static final String CRITERION = "read";
    private static final String[] TITLES = {
            "The First Fragment", "The Second Fragment", "The Third Fragment", "The Final Fragment"
    };

    public LostPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        PlayerAdvancements progress = serverPlayer.getAdvancements();

        // Find the first page this player hasn't read yet
        for (int i = 0; i < TITLES.length; i++) {
            AdvancementHolder page = serverPlayer.server.getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "lost_page_" + (i + 1)));
            if (page == null || progress.getOrStartProgress(page).isDone()) continue;

            progress.award(page, CRITERION);
            if (!player.getAbilities().instabuild) stack.shrink(1);

            MagicData data = MagicHelper.get(serverPlayer);
            MagicHelper.set(serverPlayer, data.withKnowledge(data.knowledge() + KNOWLEDGE_REWARD));

            serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.lost_page_item.you_piece_together_a_lost_page", TITLES[i])
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            serverPlayer.sendSystemMessage(Component.translatable("hollowmancy.lost_page_item.knowledge_its_words_are_now_in", KNOWLEDGE_REWARD)
                    .withStyle(ChatFormatting.GRAY));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.15f, 1.6f);
            return InteractionResultHolder.consume(stack);
        }

        serverPlayer.displayClientMessage(Component.translatable("hollowmancy.lost_page_item.every_page_is_already_in_your")
                .withStyle(ChatFormatting.GRAY), true);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.lost_page_item.torn_from_a_journal_much_like").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("hollowmancy.lost_page_item.right_click_to_read").withStyle(ChatFormatting.GRAY));
    }
}