package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Combos {

    public static final int CHILL_TICKS = 100;   // Frost Nova's mark lasts 5 seconds
    public static final int HURL_TICKS = 30;     // Repel's mark lasts 1.5 seconds
    public static final float SHATTER_MULTIPLIER = 2.0f;
    public static final float THERMAL_SHOCK_BONUS = 6.0f;  // 3 hearts
    public static final float SLAM_DAMAGE = 4.0f;          // 2 hearts
    public static final int SLAM_STUN_TICKS = 60;          // 3 seconds
    public static final float EXPOSED_MULTIPLIER = 1.25f;

    private record Hurled(ServerPlayer caster, long until) {}

    // Weak maps: a creature that dies or unloads simply drops out
    private static final Map<LivingEntity, Long> CHILLED = new WeakHashMap<>();
    private static final Map<LivingEntity, Hurled> HURLED = new WeakHashMap<>();

    // ---------------- Marks ----------------

    public static void chill(LivingEntity target) {
        CHILLED.put(target, target.level().getGameTime() + CHILL_TICKS);
    }

    public static boolean isChilled(LivingEntity target) {
        Long until = CHILLED.get(target);
        return until != null && target.level().getGameTime() <= until;
    }

    private static void breakChill(LivingEntity target) {
        CHILLED.remove(target);
        target.setTicksFrozen(0);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    public static void hurl(LivingEntity target, ServerPlayer caster) {
        HURLED.put(target, new Hurled(caster, target.level().getGameTime() + HURL_TICKS));
    }

    // ---------------- Shatter (called by Spark and Void Lance) ----------------

    /** Returns the damage to deal: doubled, with effects, if the target is chilled. */
    public static float shatter(ServerPlayer caster, LivingEntity target, float damage) {
        if (!isChilled(target)) return damage;
        breakChill(target);

        ServerLevel level = caster.serverLevel();
        level.sendParticles(HollowmancyParticles.FROST_SHARD.get(),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 30, 0.4, 0.5, 0.4, 0.2);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2f, 0.8f);
        announce(caster, "SHATTER", 0xAADDFF);
        return damage * SHATTER_MULTIPLIER;
    }

    // ---------------- Thermal Shock & Exposed (any player damage) ----------------

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer caster)) return;
        LivingEntity target = event.getEntity();
        if (target == caster) return;

        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        boolean magical = fire || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypeTags.IS_LIGHTNING);

        // Thermal Shock: fire against a chilled creature
        if (fire && isChilled(target)) {
            breakChill(target);
            event.setAmount(event.getAmount() + THERMAL_SHOCK_BONUS);
            ServerLevel level = caster.serverLevel();
            level.sendParticles(HollowmancyParticles.WIND.get(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    25, 0.4, 0.5, 0.4, 0.08);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2f, 0.7f);
            announce(caster, "THERMAL SHOCK", 0xFF7A3D);
        }

        // Exposed: magic against a glowing creature
        if (magical && target.hasEffect(MobEffects.GLOWING)) {
            event.setAmount(event.getAmount() * EXPOSED_MULTIPLIER);
            caster.serverLevel().sendParticles(HollowmancyParticles.ARCANE_SPARK.get(),
                    target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 6, 0.3, 0.2, 0.3, 0.1);
        }
    }

    // ---------------- Slam (checked every tick) ----------------

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (HURLED.isEmpty()) return;

        for (Map.Entry<LivingEntity, Hurled> entry : new ArrayList<>(HURLED.entrySet())) {
            LivingEntity target = entry.getKey();
            Hurled hurled = entry.getValue();

            if (target.isRemoved() || !target.isAlive() || target.level().getGameTime() > hurled.until()) {
                HURLED.remove(target);
                continue;
            }
            if (!target.horizontalCollision) continue;

            // It hit a wall
            HURLED.remove(target);
            ServerPlayer caster = hurled.caster();
            DamageSource source = caster.isRemoved()
                    ? target.damageSources().generic()
                    : caster.damageSources().indirectMagic(caster, caster);
            target.hurt(source, SLAM_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLAM_STUN_TICKS, 4));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SLAM_STUN_TICKS, 1));
            if (target instanceof Mob mob) mob.getNavigation().stop();

            if (target.level() instanceof ServerLevel level) {
                SpellFx.ring(level, HollowmancyParticles.RING_ARCANE.get(), target.position().add(0, 0.1, 0), 2.2F);
                level.sendParticles(HollowmancyParticles.RUNE_ARCANE.get(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        4, 0.3, 0.3, 0.3, 0.02);
                level.sendParticles(HollowmancyParticles.ARCANE_SPARK.get(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        15, 0.4, 0.5, 0.4, 0.2);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 0.6f);
            }
            if (!caster.isRemoved()) announce(caster, "SLAM", 0x5DA9E9);
        }
    }

    // ---------------- Feedback ----------------

    private static void announce(ServerPlayer caster, String name, int color) {
        caster.displayClientMessage(Component.literal(name)
                .withStyle(style -> style.withColor(TextColor.fromRgb(color)).withBold(true)), true);
        caster.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 0.6f);
    }
}