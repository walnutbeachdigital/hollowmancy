package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class SpiritWolves {

    /** Saved on the wolf itself, so it survives the chunk unloading or the world closing. */
    private static final String EXPIRES_KEY = "hollowmancy_spirit_expires";

    public static void mark(Wolf wolf, long expiresAtGameTime) {
        wolf.getPersistentData().putLong(EXPIRES_KEY, expiresAtGameTime);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if (!(wolf.level() instanceof ServerLevel level)) return;
        if (!wolf.getPersistentData().contains(EXPIRES_KEY)) return;

        if (level.getGameTime() >= wolf.getPersistentData().getLong(EXPIRES_KEY)) {
            level.sendParticles(HollowmancyParticles.LEAF.get(), wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                    12, 0.3, 0.4, 0.3, 0.0);
            level.sendParticles(HollowmancyParticles.RUNE_NATURE.get(), wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(),
                    SoundEvents.WOLF_WHINE, SoundSource.NEUTRAL, 0.8f, 1.2f);
            wolf.discard(); // fades away: no drops, no death message
        }
    }
}