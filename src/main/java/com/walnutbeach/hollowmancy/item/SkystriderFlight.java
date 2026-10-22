package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.spell.Soaring;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Skystrider Boots: flight while worn, paid in mana. */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class SkystriderFlight {

    /** Saved on the player while the boots are the reason they can fly. */
    private static final String KEY = "hollowmancy_skystrider";
    public static final int DRAIN_PER_SECOND = 2;

    private static boolean wearingBoots(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(Hollowmancy.SKYSTRIDER_BOOTS.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        boolean granted = player.getPersistentData().getBoolean(KEY);

        if (player.isCreative() || player.isSpectator()) {
            if (granted) player.getPersistentData().remove(KEY);
            return;
        }
        if (Soaring.isSoaring(player)) return; // the Soar spell is already handling flight

        Abilities abilities = player.getAbilities();
        MagicData data = MagicHelper.get(player);
        boolean canFly = wearingBoots(player) && data.mana() >= DRAIN_PER_SECOND;

        if (!canFly) {
            if (granted) land(player, wearingBoots(player) ? "Your boots falter. The wind lets you down gently." : null);
            return;
        }

        if (!abilities.mayfly) {
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }
        player.getPersistentData().putBoolean(KEY, true);

        if (!abilities.flying) return; // walking costs nothing

        if (player.tickCount % 4 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(),
                    1, 0.15, 0.02, 0.15, 0.0);
        }
        if (player.tickCount % 20 == 0) {
            MagicHelper.set(player, data.withMana(data.mana() - DRAIN_PER_SECOND));
        }
    }

    private static void land(ServerPlayer player, String message) {
        player.getPersistentData().remove(KEY);
        Abilities abilities = player.getAbilities();
        boolean airborne = abilities.flying || !player.onGround();
        abilities.mayfly = false;
        abilities.flying = false;
        player.onUpdateAbilities();
        if (airborne) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        if (message != null) player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GRAY), true);
    }
}