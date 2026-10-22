package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * No vanilla mobs in the Hollow. Natural spawns, chunk generation, structures, spawners and patrols
 * are all refused. Spawn eggs and /summon still work, so you can bring one in on purpose.
 * Vanilla monsters already saved in the Hollow (Endermen and Drowned from older worlds) vanish when
 * their chunk loads, unless they have a name tag.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class HollowSpawnRules {
    public static final ResourceKey<Level> HOLLOW =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "the_hollow"));
    private static final String ALLOWED = "hollowmancy.allowed_in_hollow";

    public static boolean isVanilla(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("minecraft");
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel().getLevel().dimension() != HOLLOW) return;
        if (!isVanilla(event.getEntity())) {
            thinOut(event);
            return;
        }
        switch (event.getSpawnType()) {
            case SPAWN_EGG, COMMAND, BREEDING, BUCKET, DISPENSER, CONVERSION -> event.getEntity().addTag(ALLOWED);
            default -> event.setSpawnCancelled(true);
        }
    }

    /** The spawn-rate settings: keep only that share of the Hollow creatures' natural spawns. */
    private static void thinOut(FinalizeSpawnEvent event) {
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION) return;
        if (!BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getNamespace().equals(Hollowmancy.MODID)) return;
        double rate = event.getEntity().getType().getCategory() == MobCategory.MONSTER
                ? HollowmancyExtraConfig.get(HollowmancyExtraConfig.HOSTILE_SPAWN_RATE)
                : HollowmancyExtraConfig.get(HollowmancyExtraConfig.AMBIENT_SPAWN_RATE);
        if (rate < 1.0 && event.getEntity().getRandom().nextDouble() >= rate) event.setSpawnCancelled(true);
    }

    /** Catches the few ways monsters appear without a spawn check (an Ender Pearl's endermite), and old saved ones. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != HOLLOW) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob) || !(entity instanceof Enemy) || !isVanilla(entity)) return;
        if (mob.getTags().contains(ALLOWED) || mob.hasCustomName() || mob.isPersistenceRequired()) return;
        event.setCanceled(true);
    }
}
