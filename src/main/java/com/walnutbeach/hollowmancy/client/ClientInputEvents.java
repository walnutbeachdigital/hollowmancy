package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import com.walnutbeach.hollowmancy.network.CastSpellPayload;
import com.walnutbeach.hollowmancy.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hollowmancy.MODID, value = Dist.CLIENT)
public class ClientInputEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        MagicData data = mc.player.getData(ModAttachments.MAGIC);

        while (ModKeybinds.CAST_SPELL.consumeClick()) {
            Spell spell = ClientSpellState.getSelected(data);
            if (spell == null) continue;
            tryCast(mc, spell);
        }

        while (ModKeybinds.CYCLE_SPELL.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new SpellWheelScreen(data));
        }

        while (ModKeybinds.OPEN_SPELLBOOK.consumeClick()) {
            if (data.stage() < MagicStage.AWAKENING) continue;
            if (mc.player.getInventory().contains(new ItemStack(Hollowmancy.SPELLBOOK.get()))) {
                SpellbookClient.open();
            } else {
                mc.player.displayClientMessage(Component.translatable("hollowmancy.client_input_events.you_need_your_spellbook_with_you")
                        .withStyle(ChatFormatting.GRAY), true);
            }
        }
    }

    /** Shared by the R key and the Focus Wand: refuse locally if recharging, otherwise ask the server. */
    public static void tryCast(Minecraft mc, Spell spell) {
        int remaining = ClientSpellState.cooldownRemaining(spell.getId(), mc.level.getGameTime());
        if (remaining > 0 && !ClientSpellState.isChanneling()) {
            mc.player.displayClientMessage(Component.translatable("hollowmancy.focus_wand_item.is_recharging_s", spell.getName(), String.format("%.1f", remaining / 20.0))
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        PacketDistributor.sendToServer(new CastSpellPayload(spell.getId()));
    }

    // Forget cooldowns when leaving a world, so they don't carry into a different one
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSpellState.clearCooldowns();
    }
}