package com.walnutbeach.hollowmancy.item.hollow;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * What the charms do.
 *   Wispbound:  mana regenerates faster (see ArcanistGear.regen)
 *   Mothwing:   you drift down instead of falling (hold Shift to drop)
 *   Huntsman's: hostile creatures near you glow, so magic hits them harder ("Exposed")
 *   Shadeweave: when you drop below 30% health you vanish for a few seconds and enemies lose you
 *   Tidebound:  breathe and swim freely underwater
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class HollowCharms {
    public static final float WISPBOUND_REGEN = 1.25F;
    public static final double HUNTSMAN_RANGE = 14.0;
    public static final float SHADEWEAVE_HEALTH = 0.30F;
    public static final int SHADEWEAVE_INVISIBLE_TICKS = 100;
    public static final int SHADEWEAVE_COOLDOWN_TICKS = 1200;
    private static final String SHADEWEAVE_READY = "hollowmancy.shadeweave_ready";

    /** For ArcanistGear.regen: the Wispbound Charm's boost (always at least +1). */
    public static int regen(net.minecraft.world.entity.player.Player player, int regen) {
        if (regen <= 0 || !HollowCharmItem.has(player, HollowCharmItem.Kind.WISPBOUND)) return regen;
        return Math.max(regen + 1, Math.round(regen * WISPBOUND_REGEN));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0 || player.isSpectator()) return;

        if (!player.onGround() && player.fallDistance > 2.5F && !player.isShiftKeyDown() && !player.isFallFlying()
                && !player.getAbilities().flying && HollowCharmItem.has(player, HollowCharmItem.Kind.MOTHWING)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false, true));
        }

        if (player.isInWater() && HollowCharmItem.has(player, HollowCharmItem.Kind.TIDEBOUND)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, 0, true, false, true));
        }

        if (player.tickCount % 40 == 0 && HollowCharmItem.has(player, HollowCharmItem.Kind.HUNTSMAN)) {
            for (LivingEntity mob : player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(HUNTSMAN_RANGE),
                    e -> e instanceof Enemy && e.isAlive())) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, false));
            }
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        if (player.getHealth() > player.getMaxHealth() * SHADEWEAVE_HEALTH) return;
        long now = player.serverLevel().getGameTime();
        if (now < player.getPersistentData().getLong(SHADEWEAVE_READY)) return;
        if (!HollowCharmItem.has(player, HollowCharmItem.Kind.SHADEWEAVE)) return;

        player.getPersistentData().putLong(SHADEWEAVE_READY, now + SHADEWEAVE_COOLDOWN_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, SHADEWEAVE_INVISIBLE_TICKS, 0, false, false, true));
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(20.0), m -> m.getTarget() == player)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        SpellFx.burst(player.serverLevel(), HollowmancyParticles.VOID_WISP.get(), player.position().add(0, 1.0, 0), 30, 0.4, 0.05);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 0.7F);
        player.displayClientMessage(Component.translatable("item.hollowmancy.shadeweave_charm.triggered").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    private HollowCharms() {}
}
