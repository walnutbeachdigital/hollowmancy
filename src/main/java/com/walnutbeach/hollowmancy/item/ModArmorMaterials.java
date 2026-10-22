package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Hollowmancy.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCANIST = ARMOR_MATERIALS.register("arcanist",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 2);
                        map.put(ArmorItem.Type.CHESTPLATE, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 3);
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.BODY, 4);
                    }),
                    25,                                      // enchantability (gold is 25, iron is 9)
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(Items.PURPLE_WOOL),  // repair material
                    // Draws textures/models/armor/arcanist_layer_1.png and _layer_2.png
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcanist"))),
                    0.0f,                                    // toughness
                    0.0f                                     // knockback resistance
            ));

    /** For fused garb: tougher, with its own worn look (textures/models/armor/arcanist_regalia_layer_1.png). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCANIST_REGALIA = ARMOR_MATERIALS.register("arcanist_regalia",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.CHESTPLATE, 6);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.BOOTS, 2);
                        map.put(ArmorItem.Type.BODY, 6);
                    }),
                    25,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(Items.PURPLE_WOOL),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcanist_regalia"))),
                    1.0f,
                    0.0f
            ));

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }
}