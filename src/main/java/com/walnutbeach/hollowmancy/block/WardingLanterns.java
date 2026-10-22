package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class WardingLanterns {

    public static final int RADIUS = 24;

    private static final Map<ResourceKey<Level>, Set<BlockPos>> LIT = new HashMap<>();

    public static void setLit(Level level, BlockPos pos, boolean lit) {
        if (level.isClientSide) return;
        Set<BlockPos> lanterns = LIT.computeIfAbsent(level.dimension(), key -> new HashSet<>());
        if (lit) lanterns.add(pos.immutable());
        else lanterns.remove(pos);
    }

    public static boolean isWarded(Level level, BlockPos pos) {
        Set<BlockPos> lanterns = LIT.get(level.dimension());
        if (lanterns == null) return false;
        for (BlockPos lantern : lanterns) {
            if (lantern.distSqr(pos) <= RADIUS * RADIUS) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;

        // Only natural spawns. Spawners, eggs and commands still work.
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.PATROL && type != MobSpawnType.CHUNK_GENERATION) return;

        if (isWarded(event.getLevel().getLevel(), event.getEntity().blockPosition())) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) LIT.remove(level.dimension());
    }
}