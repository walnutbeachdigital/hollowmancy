package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Arcanist's Garb, rewoven at a Smithing Table with Hollow materials. Each piece keeps its garb bonus
 * (and counts toward the full set) and gains something of the creature it was made from.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class GarbUpgrades {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    public static final DeferredItem<ArcanistArmorItem> WISPWOVEN_HOOD = ITEMS.register("wispwoven_hood",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(25)).rarity(Rarity.RARE),
                    "Spells cost 10% less mana\nYou see in the dark"));
    public static final DeferredItem<ArcanistArmorItem> SHADEWEAVE_ROBE = ITEMS.register("shadeweave_robe",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(25)).rarity(Rarity.RARE),
                    "Forbidden spells leave 25% less corruption\nYou vanish while sneaking"));
    public static final DeferredItem<ArcanistArmorItem> LURKERSCALE_LEGGINGS = ITEMS.register("lurkerscale_leggings",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(25)).rarity(Rarity.RARE),
                    "Mana returns 50% faster\nBreathe and swim freely underwater"));
    public static final DeferredItem<ArcanistArmorItem> MOTHSILK_BOOTS = ITEMS.register("mothsilk_boots",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(25)).rarity(Rarity.RARE),
                    "Channeled spells charge 25% faster\nYou jump higher and never take fall damage"));

    private static final List<DeferredItem<ArcanistArmorItem>> ALL = List.of(WISPWOVEN_HOOD, SHADEWEAVE_ROBE, LURKERSCALE_LEGGINGS, MOTHSILK_BOOTS);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addToTab(CreativeModeTab.Output output) {
        ALL.forEach(item -> output.accept(item.get()));
    }

    private static boolean wearing(Player player, EquipmentSlot slot, DeferredItem<ArcanistArmorItem> item) {
        return player.getItemBySlot(slot).is(item.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        if (wearing(player, EquipmentSlot.HEAD, WISPWOVEN_HOOD)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false, true));
        }
        if (wearing(player, EquipmentSlot.CHEST, SHADEWEAVE_ROBE) && player.isShiftKeyDown()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, true));
        }
        if (wearing(player, EquipmentSlot.LEGS, LURKERSCALE_LEGGINGS) && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, 0, true, false, true));
        }
        if (wearing(player, EquipmentSlot.FEET, MOTHSILK_BOOTS)) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30, 1, true, false, true));
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && wearing(player, EquipmentSlot.FEET, MOTHSILK_BOOTS)) {
            event.setCanceled(true);
        }
    }

    private GarbUpgrades() {}
}
