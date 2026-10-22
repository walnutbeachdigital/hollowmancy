package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import com.walnutbeach.hollowmancy.entity.hollow.HollowSummons;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class HollowCultist extends SpellcasterMob {

    private static final int MAX_SHADES = 2;
    private int summonCooldown = 200;

    public HollowCultist(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected int castInterval() {
        return 70; // a Void Lance every 3.5 seconds
    }

    @Override
    protected float castRange() {
        return 18.0f;
    }

    // Void Lance: heavy damage, and it corrupts players
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel level)) return;
        castFlash(level, ParticleTypes.SCULK_SOUL);
        beam(level, ParticleTypes.SQUID_INK, target.getEyePosition());
        beam(level, ParticleTypes.REVERSE_PORTAL, target.getEyePosition());
        target.hurt(damageSources().indirectMagic(this, this), 6.0f);

        if (target instanceof ServerPlayer player) {
            MagicData data = MagicHelper.get(player);
            MagicHelper.set(player, data.withCorruption(data.corruption() + 2));
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.4f, 1.8f);
    }

    // Calls shades to fight beside it
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (summonCooldown > 0) {
            summonCooldown--;
            return;
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || !(level() instanceof ServerLevel level)) return;
        summonCooldown = 300;

        // In the Hollow this is a Hollow Shade (vanilla Vexes aren't allowed there); elsewhere, a Vex
        if (HollowSummons.count(level, this, 16) >= MAX_SHADES) return;
        double angle = random.nextDouble() * Math.PI * 2;
        if (HollowSummons.summon(level, this, target, getX() + Math.cos(angle) * 1.5, getY() + 1.5, getZ() + Math.sin(angle) * 1.5, 20 * 25) == null) return;

        level.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.5, getZ(), 12, 0.3, 0.3, 0.3, 0.03);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 0.8f);
    }
}