package com.walnutbeach.hollowmancy.client;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.ArcanistArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Arcanist's Garb worn in 3D: a peaked hood with a drape, a high collar, shoulder pieces and coat tails,
 * boot cuffs, and a sash on the leggings. The shapes share part names with the player, so they move with you.
 * Textures: textures/models/armor/(arcanist|arcanist_regalia)_layer_(1|2)_3d.png (64x64).
 */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArcanistArmorClient {
    public static final ModelLayerLocation OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcanist_armor"), "outer");
    public static final ModelLayerLocation INNER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcanist_armor"), "inner");

    private static final float DEG = (float) (Math.PI / 180.0);

    /** Hood, robe and boots. */
    public static LayerDefinition createOuter() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("hood_peak", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, -3.0F, 0.0F, 4, 3, 4, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, -8.5F, 1.0F, -28.6F * DEG, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_drape", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, 0.0F, 8, 6, 1, new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, -1.0F, 4.6F));

        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 40).addBox(-4.5F, -1.5F, -2.5F, 9, 2, 5, new CubeDeformation(1.2F)),
                PartPose.ZERO);
        body.addOrReplaceChild("tail_back", CubeListBuilder.create().texOffs(28, 40).addBox(-4.0F, 0.0F, 0.0F, 8, 9, 1, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 2.6F, 8.6F * DEG, 0.0F, 0.0F));
        body.addOrReplaceChild("tail_front_r", CubeListBuilder.create().texOffs(46, 40).addBox(-2.0F, 0.0F, -1.0F, 4, 7, 1, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(-2.5F, 12.0F, -2.8F, -5.7F * DEG, 0.0F, 0.0F));
        body.addOrReplaceChild("tail_front_l", CubeListBuilder.create().texOffs(46, 40).addBox(-2.0F, 0.0F, -1.0F, 4, 7, 1, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(2.5F, 12.0F, -2.8F, -5.7F * DEG, 0.0F, 0.0F));

        root.getChild("right_arm").addOrReplaceChild("pauldron_r", CubeListBuilder.create().texOffs(0, 50)
                .addBox(-4.0F, -3.5F, -3.0F, 5, 3, 6, new CubeDeformation(0.4F)), PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("pauldron_l", CubeListBuilder.create().texOffs(22, 50)
                .addBox(-1.0F, -3.5F, -3.0F, 5, 3, 6, new CubeDeformation(0.4F)), PartPose.ZERO);

        for (String leg : new String[]{"right_leg", "left_leg"}) {
            root.getChild(leg).addOrReplaceChild(leg + "_cuff", CubeListBuilder.create().texOffs(44, 50)
                    .addBox(-2.5F, 7.0F, -2.5F, 5, 2, 5, new CubeDeformation(0.9F)), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Leggings: a sash at the waist with a hanging end. */
    public static LayerDefinition createInner() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F);
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5F, 9.5F, -2.5F, 9, 2, 5, new CubeDeformation(0.7F)),
                PartPose.ZERO);
        body.addOrReplaceChild("sash_tail", CubeListBuilder.create().texOffs(28, 32).addBox(0.0F, 0.0F, 0.0F, 2, 5, 1, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(1.0F, 11.5F, -3.6F, -6.0F * DEG, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OUTER, ArcanistArmorClient::createOuter);
        event.registerLayerDefinition(INNER, ArcanistArmorClient::createInner);
    }

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        Item[] garb = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof ArcanistArmorItem).toArray(Item[]::new);
        if (garb.length == 0) return;
        event.registerItem(new IClientItemExtensions() {
            private HumanoidModel<LivingEntity> outer, inner;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (outer == null) {
                    EntityModelSet models = Minecraft.getInstance().getEntityModels();
                    outer = new HumanoidModel<>(models.bakeLayer(OUTER));
                    inner = new HumanoidModel<>(models.bakeLayer(INNER));
                }
                return slot == EquipmentSlot.LEGS ? inner : outer;
            }
        }, garb);
    }
}
