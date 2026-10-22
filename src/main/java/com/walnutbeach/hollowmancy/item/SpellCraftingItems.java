package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

/** Everything for spell crafting: rune stones, the nine runes, and scrolls. */
public class SpellCraftingItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    public static final DeferredItem<Item> RUNE_STONE = ITEMS.registerSimpleItem("rune_stone");

    public static final DeferredItem<RuneItem> BOLT_RUNE = rune("bolt_rune", "Shape: a bolt that flies where you aim");
    public static final DeferredItem<RuneItem> NOVA_RUNE = rune("nova_rune", "Shape: a burst all around you");
    public static final DeferredItem<RuneItem> BEAM_RUNE = rune("beam_rune", "Shape: a beam that pierces a whole line");
    public static final DeferredItem<RuneItem> FIRE_RUNE = rune("fire_rune", "Element: fire (Elemental)");
    public static final DeferredItem<RuneItem> FROST_RUNE = rune("frost_rune", "Element: frost (Elemental)");
    public static final DeferredItem<RuneItem> VOID_RUNE = rune("void_rune", "Element: void (Forbidden)");
    public static final DeferredItem<RuneItem> SPLIT_RUNE = rune("split_rune", "Modifier: splitting, three at once");
    public static final DeferredItem<RuneItem> HOMING_RUNE = rune("homing_rune", "Modifier: seeking, finds its target");
    public static final DeferredItem<RuneItem> LINGERING_RUNE = rune("lingering_rune", "Modifier: lingering, leaves the ground burning");

    public static final DeferredItem<Item> BLANK_SCROLL = ITEMS.registerSimpleItem("blank_scroll");
    public static final DeferredItem<InscribedScrollItem> INSCRIBED_SCROLL = ITEMS.register("inscribed_scroll",
        () -> new InscribedScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RecipeSerializer<SpellScrollRecipe> SCROLL_RECIPE = new SimpleCraftingRecipeSerializer<>(SpellScrollRecipe::new);

    private static DeferredItem<RuneItem> rune(String name, String text) {
        return ITEMS.register(name, () -> new RuneItem(new Item.Properties().rarity(Rarity.UNCOMMON), text));
    }

    /** Called from Hollowmancy's constructor. */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void fillCreativeTab(CreativeModeTab.Output output) {
        for (var item : ITEMS.getEntries()) {
            if (item.get() != INSCRIBED_SCROLL.get()) output.accept(item.get());
        }
    }

    @EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.RECIPE_SERIALIZER, helper ->
                helper.register(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "spell_scroll"), SCROLL_RECIPE));
        }
    }

    public static class RuneItem extends Item {
        private final String text;

        public RuneItem(Properties properties, String text) {
            super(properties);
            this.text = text;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal(text).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("hollowmancy.spell_crafting_items.craft_with_a_blank_scroll_to").withStyle(ChatFormatting.GRAY));
        }
    }
}
