package com.walnutbeach.hollowmancy.familiar;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Divination. Sees in the dark, spots monsters, warns you when they close in, and catches you when you fall. */
public class OwlFamiliar extends FamiliarEntity {

    public OwlFamiliar(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public FamiliarType getFamiliarType() {
        return FamiliarType.OWL;
    }

    private static int glowRadius(int level) {
        return 8 + level * 4;
    }

    /** How close a monster gets before the owl hoots a warning (blocks). */
    private static int warnRadius(int level) {
        return 10 + level * 2;
    }

    /** Ticks before the same monster is warned about again. */
    private static final int WARN_AGAIN_TICKS = 400;
    private final Map<LivingEntity, Long> warned = new WeakHashMap<>();
    private static final String[] DIRECTIONS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

    @Override
    public String describe(int level) {
        StringBuilder text = new StringBuilder(FamiliarText.tr("hollowmancy.familiar.owl.describe", "Night Vision; hoots when monsters close in"));
        if (level >= 2) text.append(FamiliarText.tr("hollowmancy.familiar.owl.describe.glow", "; monsters within %s blocks glow", glowRadius(level)));
        if (level >= 4) text.append(FamiliarText.tr("hollowmancy.familiar.owl.describe.catch", "; catches you when you fall"));
        return text.toString();
    }

    @Override
    protected void blessOwner(ServerPlayer owner, int level) {
        owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false));
        if (level >= 2) {
            int radius = glowRadius(level);
            for (LivingEntity mob : level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(radius),
                    entity -> entity instanceof Enemy && entity.isAlive())) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerPlayer owner = ownerPlayer();
        if (owner == null) return;

        if (tickCount % 10 == 0) warnOwner(owner);

        // Level 4+: a long fall gets caught
        if (getBondLevel() >= 4 && owner.fallDistance > 5.0f && !owner.isFallFlying()) {
            owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false));
        }

        if (isOrderedToSit()) {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            return;
        }
        // Perch in the air just behind the owner's right shoulder
        double yaw = Math.toRadians(owner.getYRot());
        Vec3 shoulder = new Vec3(-Math.cos(yaw) * 0.9 + Math.sin(yaw) * 0.5, 1.9, -Math.sin(yaw) * 0.9 - Math.cos(yaw) * 0.5);
        hoverToward(owner.position().add(shoulder));
        getLookControl().setLookAt(owner, 10.0f, 10.0f);
    }

    /** Hoot when monsters come close: what, which way, and how far. */
    private void warnOwner(ServerPlayer owner) {
        long now = level().getGameTime();
        int radius = warnRadius(getBondLevel());
        List<LivingEntity> close = level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(radius),
                e -> e instanceof Enemy && e.isAlive() && !e.isInvisible() && e.distanceToSqr(owner) <= radius * radius);
        LivingEntity nearest = null;
        int fresh = 0;
        for (LivingEntity mob : close) {
            Long last = warned.get(mob);
            if (last != null && now - last < WARN_AGAIN_TICKS) continue;
            boolean coming = !(mob instanceof Mob m) || m.getTarget() == owner || mob.distanceToSqr(owner) < 8 * 8;
            if (!coming) continue;
            fresh++;
            warned.put(mob, now);
            if (nearest == null || mob.distanceToSqr(owner) < nearest.distanceToSqr(owner)) nearest = mob;
        }
        if (nearest == null) return;

        double dx = nearest.getX() - owner.getX(), dz = nearest.getZ() - owner.getZ();
        int sector = (int) Math.floorMod(Math.round(Math.toDegrees(Math.atan2(-dx, dz)) / 45.0), 8);
        Component direction = Component.translatable("hollowmancy.direction." + DIRECTIONS[sector]);
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        Component message = fresh > 1
                ? Component.translatable("hollowmancy.familiar.owl.warning_many", fresh, nearest.getName(), direction, distance)
                : Component.translatable("hollowmancy.familiar.owl.warning", nearest.getName(), direction, distance);
        owner.displayClientMessage(message.copy().withStyle(ChatFormatting.GOLD), true);
        owner.playNotifySound(SoundEvents.PARROT_AMBIENT, SoundSource.NEUTRAL, 0.9f, 0.55f);
        getLookControl().setLookAt(nearest, 30.0f, 30.0f);
    }
}
