package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/** Pure mana. Refills yours, lights the dark around you, and at its strongest, slowly washes corruption away. */
public class WispFamiliar extends FamiliarEntity {

    public WispFamiliar(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public FamiliarType getFamiliarType() {
        return FamiliarType.WISP;
    }

    /** Where its light is, so it can be taken away again when the wisp moves on. */
    private BlockPos lightAt = null;

    private static int lightLevel(int level) {
        return Math.min(15, 9 + level);   // 10 to 14
    }

    private static int manaPerPulse(int level) {
        return (level + 1) / 2;   // 1, 1, 2, 2, 3 mana every 2 seconds
    }

    @Override
    public String describe(int level) {
        String text = FamiliarText.tr("hollowmancy.familiar.wisp.describe", "+%s mana every 2 seconds; lights the dark around you", manaPerPulse(level));
        if (level >= 4) text += FamiliarText.tr("hollowmancy.familiar.wisp.describe.cleanse", "; washes away 1 corruption every 30 seconds");
        return text;
    }

    @Override
    protected void blessOwner(ServerPlayer owner, int level) {
        MagicData data = MagicHelper.get(owner);
        if (data.stage() < MagicStage.AWAKENING) return;
        MagicData updated = data;
        if (data.mana() < data.maxMana()) updated = updated.withMana(data.mana() + manaPerPulse(level));
        if (level >= 4 && data.corruption() > 0 && tickCount % 600 == 0) updated = updated.withCorruption(data.corruption() - 1);
        if (updated != data) MagicHelper.set(owner, updated);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level() instanceof ServerLevel server && tickCount % 4 == 0) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.3, getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        }
        if (tickCount % 5 == 0) updateLight();
        ServerPlayer owner = ownerPlayer();
        if (owner == null) return;
        if (isOrderedToSit()) {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            return;
        }
        // Circle the owner slowly at head height
        double angle = tickCount * 0.04;
        hoverToward(owner.position().add(Math.cos(angle) * 1.3, 1.6 + Math.sin(tickCount * 0.1) * 0.15, Math.sin(angle) * 1.3));
    }

    /** In the dark (caves, night, the Hollow) the wisp carries an invisible light with it. */
    private void updateLight() {
        BlockPos here = blockPosition();
        int sky = level().getBrightness(LightLayer.SKY, here) - level().getSkyDarken();
        boolean dark = sky < 8;
        if (lightAt != null && (!dark || !lightAt.equals(here))) clearLight();
        if (dark && lightAt == null && level().getBlockState(here).isAir()) {
            level().setBlock(here, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel(getBondLevel())), 3);
            lightAt = here;
        }
    }

    private void clearLight() {
        if (lightAt != null && level().getBlockState(lightAt).is(Blocks.LIGHT)) level().removeBlock(lightAt, false);
        lightAt = null;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide) clearLight();
        super.remove(reason);
    }
}
