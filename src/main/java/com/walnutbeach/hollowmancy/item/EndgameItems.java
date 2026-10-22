package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** The three boss trophies, the Seal they make, and the Listener's spawn egg. */
public class EndgameItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    public static final DeferredItem<Trophy> SOVEREIGN_SIGIL = trophy("sovereign_sigil", "Taken from the Hollow Sovereign");
    public static final DeferredItem<Trophy> COLOSSUS_HEART = trophy("colossus_heart", "It still hums, very faintly");
    public static final DeferredItem<Trophy> MORVANES_OATH = trophy("morvanes_oath", "A broken seal. He kept his word to the end");
    public static final DeferredItem<Trophy> SEAL_OF_UNMAKING = ITEMS.register("seal_of_unmaking",
        () -> new Trophy(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
            "Offer it at the altar, in the Hollow, to call the Listener"));
    public static final DeferredItem<DeferredSpawnEggItem> LISTENER_SPAWN_EGG = ITEMS.register("listener_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.LISTENER, 0x0A0612, 0x9B5DE5, new Item.Properties()));

    private static DeferredItem<Trophy> trophy(String name, String text) {
        return ITEMS.register(name, () -> new Trophy(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), text));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void fillCreativeTab(CreativeModeTab.Output output) {
        for (var item : ITEMS.getEntries()) output.accept(item.get());
    }

    public static class Trophy extends Item {
        private final String text;

        public Trophy(Properties properties, String text) {
            super(properties);
            this.text = text;
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
