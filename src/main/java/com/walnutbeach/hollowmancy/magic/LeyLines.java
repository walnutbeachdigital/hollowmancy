package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.block.ManaBatteryBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

/**
 * Invisible rivers of mana running through the Overworld. Nothing is stored: the lines come from the
 * world seed, so every world has its own map and the same place always has the same strength.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class LeyLines {

    public static final double SCALE = 320.0;               // bigger = lines further apart
    public static final double WIDTH = 0.06;                // bigger = wider lines
    public static final float MAX_RITUAL_DISCOUNT = 0.5f;   // rituals at the heart of a line cost half
    public static final int BATTERY_CHARGE = 2;             // mana per second into a battery at full strength

    private static final DustParticleOptions LEY_DUST = new DustParticleOptions(new Vector3f(0.45f, 0.85f, 1.0f), 1.4f);
    private static final Map<UUID, Long> SIGHT_UNTIL = new HashMap<>();

    private static long cachedSeed;
    private static SimplexNoise cachedNoise;

    private static SimplexNoise noise(ServerLevel level) {
        long seed = level.getSeed();
        if (cachedNoise == null || seed != cachedSeed) {
            cachedNoise = new SimplexNoise(RandomSource.create(seed ^ 0x1E7_11E5L));
            cachedSeed = seed;
        }
        return cachedNoise;
    }

    /** 0 = no ley line, 1 = the heart of one. Always 0 outside the Overworld. */
    public static float strength(Level level, double x, double z) {
        if (!(level instanceof ServerLevel server) || !level.dimension().equals(Level.OVERWORLD)) return 0.0f;
        double v = Math.abs(noise(server).getValue(x / SCALE, z / SCALE));
        return (float) Mth.clamp(1.0 - v / WIDTH, 0.0, 1.0);
    }

    public static float strength(Level level, BlockPos pos) {
        return strength(level, pos.getX() + 0.5, pos.getZ() + 0.5);
    }

    public static int percent(float strength) {
        return Math.round(strength * 100);
    }

    // ---------------- Rituals cost less ----------------

    public static int ritualCost(Level level, BlockPos altar, int baseCost) {
        float s = strength(level, altar);
        return Math.max(1, Math.round(baseCost * (1.0f - HollowmancyConfig.LEY_RITUAL_DISCOUNT.get().floatValue() * s)));
    }

    public static void announceDiscount(ServerPlayer player, Level level, BlockPos altar) {
        float s = strength(level, altar);
        if (s < 0.05f) return;
        player.sendSystemMessage(Component.translatable("hollowmancy.ley_lines.the_ley_line_beneath_the_altar", Math.round(HollowmancyConfig.LEY_RITUAL_DISCOUNT.get().floatValue() * s * 100))
            .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }

    // ---------------- Batteries fill on their own ----------------

    public static void chargeBattery(Level level, BlockPos pos, ManaBatteryBlockEntity battery) {
        if (level == null || level.isClientSide || level.getGameTime() % 20 != 0) return;
        float s = strength(level, pos);
        if (s < 0.2f) return;
        int added = battery.insert(Math.max(1, Math.round(HollowmancyConfig.LEY_BATTERY_CHARGE.get() * s)));
        if (added > 0 && level instanceof ServerLevel server && level.getGameTime() % 40 == 0) {
            server.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5,
                5, 0.3, 0.2, 0.3, 0.6);
        }
    }

    // ---------------- Mages standing on a line ----------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        Long until = SIGHT_UNTIL.get(player.getUUID());
        if (until != null) {
            if (level.getGameTime() > until) SIGHT_UNTIL.remove(player.getUUID());
            else if (level.getGameTime() % 10 == 0) showLines(player, 20);
        }

        if (player.tickCount % 40 != 0) return;
        float s = strength(level, player.blockPosition());
        if (s < 0.3f) return;
        MagicData data = MagicHelper.get(player);
        if (data.stage() < MagicStage.AWAKENING || data.mana() >= data.maxMana()) return;
        MagicHelper.set(player, data.withMana(data.mana() + (s >= 0.7f ? 2 : 1)));
    }

    // ---------------- Seeing them (Ley Sight) ----------------

    public static void startSight(ServerPlayer player, int ticks) {
        SIGHT_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + ticks);
        showLines(player, 20);
    }

    /** Glowing dust along every line within `radius`, visible only to this player. */
    private static void showLines(ServerPlayer player, int radius) {
        ServerLevel level = player.serverLevel();
        int px = player.getBlockX(), py = player.getBlockY(), pz = player.getBlockZ();
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                if (dx * dx + dz * dz > radius * radius) continue;
                int x = px + dx, z = pz + dz;
                float s = strength(level, x + 0.5, z + 0.5);
                if (s < 0.25f) continue;
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int y = surface > py + 3 ? py : surface;   // underground: show them at your own level
                level.sendParticles(player, LEY_DUST, true, x + 0.5, y + 0.3, z + 0.5,
                    s > 0.7f ? 2 : 1, 0.4, 0.05, 0.4, 0.0);
            }
        }
    }

    /** The strongest point within `radius` (searched on a coarse grid), or null if nothing is strong. */
    public static BlockPos findStrongest(ServerLevel level, BlockPos center, int radius) {
        BlockPos best = null;
        float bestStrength = 0.5f;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dz = -radius; dz <= radius; dz += 4) {
                float s = strength(level, center.getX() + dx + 0.5, center.getZ() + dz + 0.5);
                double dist = dx * dx + dz * dz;
                // prefer stronger; among nearly-equal, prefer nearer
                if (s > bestStrength + 0.05f || (s > bestStrength - 0.05f && s >= 0.5f && dist < bestDist)) {
                    best = center.offset(dx, 0, dz);
                    bestStrength = Math.max(bestStrength, s);
                    bestDist = dist;
                }
            }
        }
        return best;
    }
}
