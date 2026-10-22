package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Applies the config's boss health and damage settings. */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class BossTuning {

    private static final String SCALED = "hollowmancy_health_scaled";

    private static double healthMultiplier(Entity entity) {
        if (entity instanceof HollowSovereign) return HollowmancyConfig.SOVEREIGN_HEALTH.get();
        if (entity instanceof AmethystColossus) return HollowmancyConfig.COLOSSUS_HEALTH.get();
        if (entity instanceof RevenantKing) return HollowmancyConfig.KING_HEALTH.get();
        if (entity instanceof TowerWizard) return HollowmancyConfig.WIZARD_HEALTH.get();
        if (entity instanceof Castellan) return HollowmancyConfig.CASTELLAN_HEALTH.get();
        if (entity instanceof Leviathan) return HollowmancyConfig.LEVIATHAN_HEALTH.get();
        if (entity instanceof Listener) return HollowmancyConfig.LISTENER_HEALTH.get();
        return -1;
    }

    private static boolean isBoss(Entity entity) {
        return entity instanceof HollowSovereign || entity instanceof AmethystColossus || entity instanceof RevenantKing
            || entity instanceof Castellan || entity instanceof Leviathan || entity instanceof Listener;
    }

    // Scale health once, the first time a boss enters the world (it's saved after that)
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity living)) return;
        double multiplier = healthMultiplier(living);
        if (multiplier < 0 || living.getPersistentData().getBoolean(SCALED)) return;
        living.getPersistentData().putBoolean(SCALED, true);
        if (multiplier == 1.0) return;
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(maxHealth.getBaseValue() * multiplier);
        living.setHealth(living.getMaxHealth());
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && isBoss(attacker)) {
            event.setAmount((float) (event.getAmount() * HollowmancyConfig.BOSS_DAMAGE.get()));
        }
    }
}
