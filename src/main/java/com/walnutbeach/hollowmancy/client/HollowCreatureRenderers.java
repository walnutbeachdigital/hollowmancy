package com.walnutbeach.hollowmancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.client.model.CrystalMothModel;
import com.walnutbeach.hollowmancy.client.model.GlimmerhornModel;
import com.walnutbeach.hollowmancy.client.model.HollowShadeModel;
import com.walnutbeach.hollowmancy.client.model.MirrorLurkerModel;
import com.walnutbeach.hollowmancy.client.model.MirrorMinnowModel;
import com.walnutbeach.hollowmancy.client.model.ShardlingModel;
import com.walnutbeach.hollowmancy.client.model.WispModel;
import com.walnutbeach.hollowmancy.entity.hollow.CrystalMoth;
import com.walnutbeach.hollowmancy.entity.hollow.Glimmerhorn;
import com.walnutbeach.hollowmancy.entity.hollow.HollowCreatures;
import com.walnutbeach.hollowmancy.entity.hollow.HollowShade;
import com.walnutbeach.hollowmancy.entity.hollow.MirrorLurker;
import com.walnutbeach.hollowmancy.entity.hollow.MirrorMinnow;
import com.walnutbeach.hollowmancy.entity.hollow.Shardling;
import com.walnutbeach.hollowmancy.entity.hollow.Wisp;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Function;

/** How the Hollow's creatures are drawn. Each has a texture and a _glow texture (drawn at full brightness). */
@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HollowCreatureRenderers {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WispModel.LAYER, WispModel::createBodyLayer);
        event.registerLayerDefinition(CrystalMothModel.LAYER, CrystalMothModel::createBodyLayer);
        event.registerLayerDefinition(MirrorMinnowModel.LAYER, MirrorMinnowModel::createBodyLayer);
        event.registerLayerDefinition(GlimmerhornModel.LAYER, GlimmerhornModel::createBodyLayer);
        event.registerLayerDefinition(HollowShadeModel.LAYER, HollowShadeModel::createBodyLayer);
        event.registerLayerDefinition(ShardlingModel.LAYER, ShardlingModel::createBodyLayer);
        event.registerLayerDefinition(MirrorLurkerModel.LAYER, MirrorLurkerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HollowCreatures.WISP.get(), ctx -> new Creature<Wisp, WispModel>(ctx,
                new WispModel(ctx.bakeLayer(WispModel.LAYER)), 0.0F, 1.0F, true, wisp -> "wisp_" + wisp.getVariant()));
        event.registerEntityRenderer(HollowCreatures.CRYSTAL_MOTH.get(), ctx -> new Creature<CrystalMoth, CrystalMothModel>(ctx,
                new CrystalMothModel(ctx.bakeLayer(CrystalMothModel.LAYER)), 0.15F, 1.0F, false, e -> "crystal_moth"));
        event.registerEntityRenderer(HollowCreatures.MIRROR_MINNOW.get(), MinnowRenderer::new);
        event.registerEntityRenderer(HollowCreatures.GLIMMERHORN.get(), ctx -> new Creature<Glimmerhorn, GlimmerhornModel>(ctx,
                new GlimmerhornModel(ctx.bakeLayer(GlimmerhornModel.LAYER)), 0.6F, 1.0F, false, e -> "glimmerhorn"));
        event.registerEntityRenderer(HollowCreatures.HOLLOW_SHADE.get(), ctx -> new Creature<HollowShade, HollowShadeModel>(ctx,
                new HollowShadeModel(ctx.bakeLayer(HollowShadeModel.LAYER)), 0.5F, 1.25F, false, e -> "hollow_shade"));
        event.registerEntityRenderer(HollowCreatures.SHARDLING.get(), ctx -> new Creature<Shardling, ShardlingModel>(ctx,
                new ShardlingModel(ctx.bakeLayer(ShardlingModel.LAYER)), 0.5F, 1.0F, false, e -> "shardling"));
        event.registerEntityRenderer(HollowCreatures.MIRROR_LURKER.get(), ctx -> new Creature<MirrorLurker, MirrorLurkerModel>(ctx,
                new MirrorLurkerModel(ctx.bakeLayer(MirrorLurkerModel.LAYER)), 0.5F, 1.0F, false, e -> "mirror_lurker"));
    }

    static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/entity/hollow/" + name + ".png");
    }

    /** A creature: its texture, a glowing layer on top, an optional size and full-brightness. Babies are drawn half size. */
    public static class Creature<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
        private final float scale;
        private final boolean fullBright;
        private final Function<T, String> name;

        public Creature(EntityRendererProvider.Context context, M model, float shadow, float scale, boolean fullBright, Function<T, String> name) {
            super(context, model, shadow);
            this.scale = scale;
            this.fullBright = fullBright;
            this.name = name;
            addLayer(new Glow<>(this, name));
        }

        @Override
        public ResourceLocation getTextureLocation(T entity) {
            return texture(name.apply(entity));
        }

        @Override
        protected void scale(T entity, PoseStack poseStack, float partialTick) {
            float s = entity.isBaby() ? scale * 0.55F : scale;
            poseStack.scale(s, s, s);
        }

        @Override
        protected int getBlockLightLevel(T entity, BlockPos pos) {
            return fullBright ? 15 : super.getBlockLightLevel(entity, pos);
        }
    }

    /** Fish wiggle as they swim and flop over on land, like cod. */
    public static class MinnowRenderer extends Creature<MirrorMinnow, MirrorMinnowModel> {
        public MinnowRenderer(EntityRendererProvider.Context context) {
            super(context, new MirrorMinnowModel(context.bakeLayer(MirrorMinnowModel.LAYER)), 0.2F, 1.0F, false, e -> "mirror_minnow");
        }

        @Override
        protected void setupRotations(MirrorMinnow entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(4.3F * Mth.sin(0.6F * bob)));
            if (!entity.isInWater()) {
                poseStack.translate(0.1F, 0.1F, -0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
        }
    }

    /** Draws <name>_glow.png at full brightness, like an Enderman's eyes. */
    public static class Glow<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final Function<T, String> name;

        public Glow(RenderLayerParent<T, M> parent, Function<T, String> name) {
            super(parent);
            this.name = name;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer glow = buffer.getBuffer(RenderType.eyes(texture(name.apply(entity) + "_glow")));
            getParentModel().renderToBuffer(poseStack, glow, 15728640, OverlayTexture.NO_OVERLAY);
        }
    }
}
