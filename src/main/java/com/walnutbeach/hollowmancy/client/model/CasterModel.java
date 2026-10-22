package com.walnutbeach.hollowmancy.client.model;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.walnutbeach.hollowmancy.entity.SpellcasterMob;
import java.util.function.Supplier;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * One model class for all the spellcasters. Each kind has its own shape (and texture), but they share
 * part names, so the same animation drives them: robes sway, hats flop, staffs raise when they cast.
 */
public class CasterModel<T extends SpellcasterMob> extends HierarchicalModel<T> {
    /** The kinds, matching the texture names: textures/entity/(kind).png */
    public static final String[] KINDS = {"tower_wizard", "fire_wizard", "frost_wizard", "void_wizard", "hollow_cultist", "rogue_apprentice"};

    public static ModelLayerLocation layer(String kind) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "caster_" + kind), "main");
    }

    public static Supplier<LayerDefinition> definition(String kind) {
        return switch (kind) {
            case "tower_wizard" -> CasterModel::createTowerWizard;
            case "fire_wizard" -> CasterModel::createFireWizard;
            case "frost_wizard" -> CasterModel::createFrostWizard;
            case "void_wizard" -> CasterModel::createVoidWizard;
            case "hollow_cultist" -> CasterModel::createHollowCultist;
            case "rogue_apprentice" -> CasterModel::createRogueApprentice;
            default -> CasterModel::createTowerWizard;
        };
    }

    private final ModelPart root, body, head, rightArm, leftArm;
    private final ModelPart skirt, cape, orb, staff, book, hatTip, hatTip2, beard, rightLeg, leftLeg;
    private final boolean reader;

    public CasterModel(ModelPart root, String kind) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
        this.skirt = child(body, "skirt");
        this.cape = child(body, "cape");
        this.orb = child(body, "orb");
        this.staff = child(rightArm, "staff");
        this.book = child(leftArm, "book");
        ModelPart hat = child(head, "hat");
        this.hatTip = child(hat, "hat_tip");
        this.hatTip2 = child(hatTip, "hat_tip2");
        this.beard = child(head, "beard");
        this.rightLeg = child(root, "right_leg");
        this.leftLeg = child(root, "left_leg");
        this.reader = book != null;
    }

    private static ModelPart child(ModelPart parent, String name) {
        return parent != null && parent.hasChild(name) ? parent.getChild(name) : null;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;

        float walk = limbSwing * 0.6662F;
        if (rightLeg != null && leftLeg != null) {
            rightLeg.xRot = Mth.cos(walk) * 1.2F * limbSwingAmount;
            leftLeg.xRot = Mth.cos(walk + Mth.PI) * 1.2F * limbSwingAmount;
        }
        rightArm.xRot += Mth.cos(walk + Mth.PI) * 0.5F * limbSwingAmount;
        leftArm.xRot += Mth.cos(walk) * 0.5F * limbSwingAmount;
        float breathe = Mth.cos(ageInTicks * 0.09F) * 0.04F;
        rightArm.zRot += breathe;
        leftArm.zRot -= breathe;

        // Robes, cloaks, hats and beards trail behind
        if (skirt != null) {
            skirt.xRot += limbSwingAmount * 0.3F + Mth.sin(ageInTicks * 0.08F) * 0.02F;
            skirt.zRot += Mth.cos(walk) * 0.05F * limbSwingAmount;
        }
        if (cape != null) cape.xRot += limbSwingAmount * 0.7F + Mth.sin(ageInTicks * 0.07F) * 0.04F;
        if (hatTip != null) hatTip.xRot -= limbSwingAmount * 0.25F + Mth.sin(ageInTicks * 0.1F) * 0.06F;
        if (hatTip2 != null) hatTip2.xRot -= Mth.sin(ageInTicks * 0.1F + 0.8F) * 0.1F;
        if (beard != null) beard.xRot += limbSwingAmount * 0.15F;
        if (orb != null) {
            orb.y += Mth.sin(ageInTicks * 0.15F) * 0.8F;
            orb.yRot = ageInTicks * 0.1F;
            orb.xRot = ageInTicks * 0.07F;
        }

        // The apprentice keeps its nose in its book
        if (reader) {
            leftArm.xRot = -0.9F + Mth.sin(ageInTicks * 0.05F) * 0.04F;
            leftArm.yRot = 0.25F;
        }

        // In a fight: staff (or empty hand) held ready, the other hand raised
        if (entity.isAggressive()) {
            rightArm.xRot = -0.55F + Mth.sin(ageInTicks * 0.2F) * 0.05F;
            if (!reader) {
                leftArm.xRot = -1.0F + Mth.cos(ageInTicks * 0.2F) * 0.05F;
                leftArm.yRot = 0.35F;
            }
            if (orb != null) orb.z -= 1.5F;
        }

        // Casting: hands thrown forward, the staff swung up and down
        if (attackTime > 0.0F) {
            float cast = Mth.sin(attackTime * Mth.PI);
            rightArm.xRot = -0.55F - 1.6F * cast;
            if (!reader) leftArm.xRot = -1.0F - 0.6F * cast;
            body.xRot -= 0.12F * cast;
            if (orb != null) orb.z -= 3.0F * cast;
        }
    }

    /** tower wizard */
    public static LayerDefinition createTowerWizard() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(80, 36).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 2F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4.5F, 0F, -3F, 9F, 8F, 6F, new CubeDeformation(grow))
                .texOffs(48, 23).addBox(-5F, 7F, -3.5F, 10F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(0, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(12, 45).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        PartDefinition p_staff = p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -12F, -0.5F, 1F, 22F, 1F, new CubeDeformation(grow))
                .texOffs(72, 45).addBox(-1F, -13F, -1F, 2F, 1F, 2F, new CubeDeformation(grow))
                .texOffs(0, 45).addBox(-1.5F, -16.5F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow))
                .texOffs(80, 45).addBox(-0.5F, -17.5F, -0.5F, 1F, 1F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-1.2F, 9F, -1F));
        p_staff.addOrReplaceChild("prong-16_0", CubeListBuilder.create()
                .texOffs(44, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-1.6F, -13F, 0F, 0F, 0F, -0.3351F));
        p_staff.addOrReplaceChild("prong16_0", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1.6F, -13F, 0F, 0F, 0F, 0.3351F));
        p_staff.addOrReplaceChild("prong0_-16", CubeListBuilder.create()
                .texOffs(52, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, -1.6F, 0.3351F, 0F, 0F));
        p_staff.addOrReplaceChild("prong0_16", CubeListBuilder.create()
                .texOffs(56, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, 1.6F, -0.3351F, 0F, 0F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(20, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(24, 45).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(40, 36).addBox(-3.5F, 0F, -0.6F, 7F, 8F, 1F, new CubeDeformation(grow))
                .texOffs(60, 45).addBox(-2.5F, 0F, -1.2F, 5F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -2F, -4.2F, 0.1396F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-6F, -1F, -6F, 12F, 1F, 12F, new CubeDeformation(grow))
                .texOffs(82, 23).addBox(-4F, -4F, -4F, 8F, 3F, 8F, new CubeDeformation(grow))
                .texOffs(56, 36).addBox(-3F, -7F, -3F, 6F, 3F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, -8F, 0F));
        PartDefinition p_hat_tip = p_hat.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                .texOffs(110, 36).addBox(-2F, -4F, -2F, 4F, 4F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -7F, 0.5F, -0.3142F, 0F, 0F));
        p_hat_tip.addOrReplaceChild("hat_tip2", CubeListBuilder.create()
                .texOffs(36, 45).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -4F, 0.5F, -0.4363F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** fire wizard */
    public static LayerDefinition createFireWizard() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(64, 36).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 2F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4.5F, 0F, -3F, 9F, 8F, 6F, new CubeDeformation(grow))
                .texOffs(48, 23).addBox(-5F, 7F, -3.5F, 10F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(0, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(16, 45).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        PartDefinition p_staff = p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -12F, -0.5F, 1F, 22F, 1F, new CubeDeformation(grow))
                .texOffs(76, 45).addBox(-1F, -13F, -1F, 2F, 1F, 2F, new CubeDeformation(grow))
                .texOffs(110, 36).addBox(-1.5F, -16.5F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow))
                .texOffs(84, 45).addBox(-0.5F, -17.5F, -0.5F, 1F, 1F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-1.2F, 9F, -1F));
        p_staff.addOrReplaceChild("prong-16_0", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-1.6F, -13F, 0F, 0F, 0F, -0.3351F));
        p_staff.addOrReplaceChild("prong16_0", CubeListBuilder.create()
                .texOffs(52, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1.6F, -13F, 0F, 0F, 0F, 0.3351F));
        p_staff.addOrReplaceChild("prong0_-16", CubeListBuilder.create()
                .texOffs(56, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, -1.6F, 0.3351F, 0F, 0F));
        p_staff.addOrReplaceChild("prong0_16", CubeListBuilder.create()
                .texOffs(60, 45).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, 1.6F, -0.3351F, 0F, 0F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(20, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(28, 45).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-3.5F, 0F, -0.6F, 7F, 6F, 1F, new CubeDeformation(grow))
                .texOffs(64, 45).addBox(-2.5F, 0F, -1.2F, 5F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -2F, -4.2F, 0.1396F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-6F, -1F, -6F, 12F, 1F, 12F, new CubeDeformation(grow))
                .texOffs(82, 23).addBox(-4F, -4F, -4F, 8F, 3F, 8F, new CubeDeformation(grow))
                .texOffs(40, 36).addBox(-3F, -7F, -3F, 6F, 3F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, -8F, 0F));
        PartDefinition p_hat_tip = p_hat.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                .texOffs(94, 36).addBox(-2F, -4F, -2F, 4F, 4F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -7F, 0.5F, -0.3142F, 0F, 0F));
        PartDefinition p_hat_tip2 = p_hat_tip.addOrReplaceChild("hat_tip2", CubeListBuilder.create()
                .texOffs(40, 45).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -4F, 0.5F, -0.4363F, 0F, 0F));
        p_hat_tip2.addOrReplaceChild("hat_ember", CubeListBuilder.create()
                .texOffs(88, 45).addBox(-0.5F, -1.5F, -0.5F, 1F, 1F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -3F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** frost wizard */
    public static LayerDefinition createFrostWizard() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(80, 36).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 2F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4.5F, 0F, -3F, 9F, 8F, 6F, new CubeDeformation(grow))
                .texOffs(48, 23).addBox(-5F, 7F, -3.5F, 10F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(16, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(12, 46).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        PartDefinition p_staff = p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -12F, -0.5F, 1F, 22F, 1F, new CubeDeformation(grow))
                .texOffs(84, 46).addBox(-1F, -13F, -1F, 2F, 1F, 2F, new CubeDeformation(grow))
                .texOffs(0, 46).addBox(-1.5F, -16.5F, -1.5F, 3F, 4F, 3F, new CubeDeformation(grow))
                .texOffs(108, 46).addBox(-0.5F, -17.5F, -0.5F, 1F, 1F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-1.2F, 9F, -1F));
        p_staff.addOrReplaceChild("prong-16_0", CubeListBuilder.create()
                .texOffs(44, 46).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-1.6F, -13F, 0F, 0F, 0F, -0.3351F));
        p_staff.addOrReplaceChild("prong16_0", CubeListBuilder.create()
                .texOffs(48, 46).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(1.6F, -13F, 0F, 0F, 0F, 0.3351F));
        p_staff.addOrReplaceChild("prong0_-16", CubeListBuilder.create()
                .texOffs(52, 46).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, -1.6F, 0.3351F, 0F, 0F));
        p_staff.addOrReplaceChild("prong0_16", CubeListBuilder.create()
                .texOffs(56, 46).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -13F, 1.6F, -0.3351F, 0F, 0F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(36, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(24, 46).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-3.5F, 0F, -0.6F, 7F, 9F, 1F, new CubeDeformation(grow))
                .texOffs(60, 46).addBox(-2.5F, 0F, -1.2F, 5F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -2F, -4.2F, 0.1396F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-6F, -1F, -6F, 12F, 1F, 12F, new CubeDeformation(grow))
                .texOffs(82, 23).addBox(-4F, -4F, -4F, 8F, 3F, 8F, new CubeDeformation(grow))
                .texOffs(56, 36).addBox(-3F, -7F, -3F, 6F, 3F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, -8F, 0F));
        PartDefinition p_hat_tip = p_hat.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                .texOffs(110, 36).addBox(-2F, -4F, -2F, 4F, 4F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -7F, 0.5F, -0.3142F, 0F, 0F));
        p_hat_tip.addOrReplaceChild("hat_tip2", CubeListBuilder.create()
                .texOffs(36, 46).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -4F, 0.5F, -0.4363F, 0F, 0F));
        p_hat.addOrReplaceChild("icicle0", CubeListBuilder.create()
                .texOffs(92, 46).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-5F, 0F, -5.5F));
        p_hat.addOrReplaceChild("icicle1", CubeListBuilder.create()
                .texOffs(72, 46).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-1.5F, 0F, -5.5F));
        p_hat.addOrReplaceChild("icicle2", CubeListBuilder.create()
                .texOffs(96, 46).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(2.5F, 0F, -5.5F));
        p_hat.addOrReplaceChild("icicle3", CubeListBuilder.create()
                .texOffs(76, 46).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offset(5F, 0F, -2F));
        p_hat.addOrReplaceChild("icicle4", CubeListBuilder.create()
                .texOffs(100, 46).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(5F, 0F, 3F));
        p_hat.addOrReplaceChild("icicle5", CubeListBuilder.create()
                .texOffs(80, 46).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow)),
            PartPose.offset(-5.5F, 0F, 2F));
        p_hat.addOrReplaceChild("icicle6", CubeListBuilder.create()
                .texOffs(104, 46).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 5.5F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** void wizard */
    public static LayerDefinition createVoidWizard() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(20, 36).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 2F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(84, 0).addBox(-4.5F, 0F, -3F, 9F, 8F, 6F, new CubeDeformation(grow))
                .texOffs(60, 22).addBox(-5F, 7F, -3.5F, 10F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(28, 22).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(94, 22).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(84, 36).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(44, 22).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(0, 36).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(96, 36).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-3.5F, -7.5F, -3.5F, 7F, 7F, 7F, new CubeDeformation(grow))
                .texOffs(22, 0).addBox(-4.5F, -8.6F, -4.5F, 9F, 9F, 9F, new CubeDeformation(0.1F + grow)),
            PartPose.offset(0F, 0F, 0F));
        p_head.addOrReplaceChild("hood_peak", CubeListBuilder.create()
                .texOffs(66, 36).addBox(-2.5F, -3F, -2F, 5F, 3F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, -8.6F, 2F, 0.5236F, 0F, 0F));
        p_body.addOrReplaceChild("orb", CubeListBuilder.create()
                .texOffs(50, 36).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(grow)),
            PartPose.offset(7.5F, 5F, -5F));
        p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, 0F, 0F, 10F, 21F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 0F, 2.6F, 0.0873F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** hollow cultist */
    public static LayerDefinition createHollowCultist() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(0, 33).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 2F, 0F, 0.2094F, 0F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-4.5F, 0F, -3F, 9F, 8F, 6F, new CubeDeformation(grow))
                .texOffs(38, 20).addBox(-5F, 7F, -3.5F, 10F, 4F, 7F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(72, 20).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(46, 33).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(70, 33).addBox(-0.5F, -1F, -0.5F, 1F, 3F, 1F, new CubeDeformation(grow))
                .texOffs(74, 33).addBox(-1.5F, -2F, -0.5F, 3F, 1F, 1F, new CubeDeformation(grow))
                .texOffs(30, 33).addBox(-0.5F, -7F, -0.5F, 1F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 9F, -1F, -1.5708F, 0F, 0F));
        p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(16, 20).addBox(-2F, -1.5F, -2F, 4F, 9F, 4F, new CubeDeformation(grow))
                .texOffs(92, 20).addBox(-2.5F, 5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(58, 33).addBox(-1.5F, 8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(94, 0).addBox(-3.5F, -7.5F, -3.5F, 7F, 7F, 7F, new CubeDeformation(grow))
                .texOffs(0, 0).addBox(-4.5F, -9F, -5F, 9F, 10F, 10F, new CubeDeformation(0.1F + grow)),
            PartPose.offsetAndRotation(0F, 0F, -0.5F, -0.2094F, 0F, 0F));
        p_head.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(34, 33).addBox(-2.5F, -3F, -0.5F, 5F, 6F, 1F, new CubeDeformation(grow)),
            PartPose.offset(0F, -4F, -3.7F));
        p_body.addOrReplaceChild("sash", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-1F, -4F, -0.5F, 2F, 11F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 3F, -2.7F, 0F, 0F, 0.5236F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** rogue apprentice */
    public static LayerDefinition createRogueApprentice() {
        float grow = 0.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 0F, -2.5F, 8F, 11F, 5F, new CubeDeformation(grow))
                .texOffs(14, 30).addBox(-4.5F, 8F, -3F, 9F, 2F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 2F, 0F));
        p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(110, 0).addBox(-2F, -1.5F, -2F, 4F, 10F, 4F, new CubeDeformation(grow))
                .texOffs(56, 16).addBox(-2.5F, 6F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(80, 30).addBox(-1.5F, 9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(-5.5F, 1.5F, 0F, 0F, 0F, 0.0873F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2F, -1.5F, -2F, 4F, 10F, 4F, new CubeDeformation(grow))
                .texOffs(76, 16).addBox(-2.5F, 6F, -2.5F, 5F, 4F, 5F, new CubeDeformation(grow))
                .texOffs(92, 30).addBox(-1.5F, 9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(5.5F, 1.5F, 0F, 0F, 0F, -0.0873F));
        p_left_arm.addOrReplaceChild("book", CubeListBuilder.create()
                .texOffs(104, 30).addBox(-3F, 0F, -2F, 6F, 1F, 4F, new CubeDeformation(grow))
                .texOffs(0, 39).addBox(-3F, -1F, -2F, 3F, 1F, 4F, new CubeDeformation(grow))
                .texOffs(14, 39).addBox(0F, -1F, -2F, 3F, 1F, 4F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 10F, -2.5F, -1.2217F, 0F, 0F));
        p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(96, 16).addBox(-4.5F, 0F, -3F, 9F, 3F, 6F, new CubeDeformation(grow)),
            PartPose.offset(0F, 11F, 0F));
        p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(26, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(grow))
                .texOffs(20, 16).addBox(-4.5F, -8.5F, -4.5F, 9F, 3F, 9F, new CubeDeformation(grow)),
            PartPose.offset(0F, 0F, 0F));
        p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-4.5F, 0F, 0F, 9F, 14F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 0F, 2.6F, 0.0698F, 0F, 0F));
        p_body.addOrReplaceChild("satchel", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-1F, -2F, -2.5F, 2F, 4F, 5F, new CubeDeformation(grow)),
            PartPose.offset(4.4F, 9F, 0F));
        p_body.addOrReplaceChild("strap", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-0.5F, -3F, -0.5F, 1F, 13F, 1F, new CubeDeformation(grow)),
            PartPose.offsetAndRotation(0F, 1F, -2.7F, 0F, 0F, -0.6109F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(78, 0).addBox(-2F, 0F, -2F, 4F, 11F, 4F, new CubeDeformation(grow))
                .texOffs(44, 30).addBox(-2F, 8F, -2.5F, 4F, 3F, 5F, new CubeDeformation(0.25F + grow)),
            PartPose.offset(-2F, 13F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(94, 0).addBox(-2F, 0F, -2F, 4F, 11F, 4F, new CubeDeformation(grow))
                .texOffs(62, 30).addBox(-2F, 8F, -2.5F, 4F, 3F, 5F, new CubeDeformation(0.25F + grow)),
            PartPose.offset(2F, 13F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

}
