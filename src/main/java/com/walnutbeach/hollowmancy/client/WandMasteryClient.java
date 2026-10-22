package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.WandMastery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/** Wand mastery on the client: the model changes with each level, and the tooltip shows progress. */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WandMasteryClient {
    public static final ResourceLocation MASTERY = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "mastery");

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Item wand : List.of(Hollowmancy.FOCUS_WAND.get(), Hollowmancy.WELLSPRING_WAND.get(), Hollowmancy.ABYSSAL_FOCUS.get())) {
                ItemProperties.register(wand, MASTERY, (stack, level, entity, seed) -> WandMastery.level(stack) / (float) WandMastery.MAX_LEVEL);
            }
        });
        NeoForge.EVENT_BUS.addListener(WandMasteryClient::tooltip);
    }

    private static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!WandMastery.isWand(stack)) return;
        int level = WandMastery.level(stack);
        List<Component> lines = event.getToolTip();
        lines.add(Component.translatable("hollowmancy.wand_mastery.tooltip.level", Component.translatable("hollowmancy.wand_mastery.level." + level))
                .withStyle(level >= WandMastery.MAX_LEVEL ? ChatFormatting.GOLD : ChatFormatting.AQUA));
        if (level > 0) {
            lines.add(Component.translatable("hollowmancy.wand_mastery.tooltip.bonus",
                    Math.round(level * WandMastery.COST_PER_LEVEL * 100), Math.round(level * WandMastery.COOLDOWN_PER_LEVEL * 100))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (level < WandMastery.MAX_LEVEL) {
            lines.add(Component.translatable("hollowmancy.wand_mastery.tooltip.next", WandMastery.toNext(stack)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
