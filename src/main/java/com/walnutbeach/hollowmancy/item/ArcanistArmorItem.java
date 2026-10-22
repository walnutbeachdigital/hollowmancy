package com.walnutbeach.hollowmancy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

public class ArcanistArmorItem extends ArmorItem {

    private final String bonus;

    public ArcanistArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, String bonus) {
        super(material, type, properties);
        this.bonus = bonus;
    }

    /** Worn, the garb uses the 3D textures (room for the hood, collar, shoulders and coat tails). */
    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        String set = getMaterial().value() == ModArmorMaterials.ARCANIST_REGALIA.get() ? "arcanist_regalia" : "arcanist";
        return ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/models/armor/" + set + (innerModel ? "_layer_2_3d.png" : "_layer_1_3d.png"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (String line : bonus.split("\n")) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("hollowmancy.arcanist_armor_item.full_arcanist_s_garb_4_pieces").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("hollowmancy.arcanist_armor_item.cooldowns_15_shorter").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hollowmancy.arcanist_armor_item.corruption_fades_3x_faster").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}