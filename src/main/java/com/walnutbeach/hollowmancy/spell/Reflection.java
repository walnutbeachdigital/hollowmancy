package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

/** What the Reflect spell does while it's active. */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Reflection {

    private static final String UNTIL = "hollowmancy_reflect_until";
    private static final String TIER = "hollowmancy_reflect_tier";

    public static void start(ServerPlayer player, int ticks, int tier) {
        player.getPersistentData().putLong(UNTIL, player.serverLevel().getGameTime() + ticks);
        player.getPersistentData().putInt(TIER, tier);
    }

    public static boolean isActive(Player player) {
        return player.getPersistentData().getLong(UNTIL) > player.level().getGameTime();
    }

    // Projectiles: turned around, and now they belong to you
    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer player) || !isActive(player)) return;
        Projectile projectile = event.getProjectile();
        if (projectile.getOwner() == player) return;

        event.setCanceled(true);
        Vec3 motion = projectile.getDeltaMovement();
        projectile.setDeltaMovement(motion.scale(-1.1));
        projectile.setOwner(player);
        projectile.hasImpulse = true;

        ServerLevel level = player.serverLevel();
        Vec3 at = hit.getLocation();
        level.sendParticles(HollowmancyParticles.WARD_GLINT.get(), at.x, at.y, at.z, 12, 0.2, 0.2, 0.2, 0.08);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.6f);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 2.0f);
    }

    // Spells cast straight at you (beams, lances): part of the damage goes back to the caster
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isActive(player)) return;
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.INDIRECT_MAGIC) && !source.is(DamageTypes.MAGIC)) return;
        if (!(source.getEntity() instanceof LivingEntity caster) || caster == player || source.getDirectEntity() != caster) return;

        float share = player.getPersistentData().getInt(TIER) >= 3 ? 1.0f : 0.5f;
        float back = event.getAmount() * share;
        event.setAmount(event.getAmount() - back);
        caster.hurt(player.damageSources().indirectMagic(player, player), back);

        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition(), path = caster.getEyePosition().subtract(from);
        SpellFx.beam(level, from, from.add(path), SpellFx.WARD, 0.14F, 10, SpellFx.SMOOTH);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 2.0f);
    }

    // A shimmer around you while it lasts
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0 || !isActive(player)) return;
        double angle = player.tickCount * 0.3;
        player.serverLevel().sendParticles(HollowmancyParticles.WARD_GLINT.get(), player.getX() + Math.cos(angle) * 0.9, player.getY() + 1.0,
            player.getZ() + Math.sin(angle) * 0.9, 1, 0, 0.1, 0, 0.0);
    }
}
