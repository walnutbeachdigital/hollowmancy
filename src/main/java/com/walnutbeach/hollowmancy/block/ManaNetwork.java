package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.fx.SpellFx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManaNetwork {

    /** A safety limit, so a giant pipe network can't stall the server. */
    private static final int MAX_CONDUITS = 256;
    /** Mana pulses run visibly through the conduits whenever mana flows. Set false to hide them. */
    public static final boolean SHOW_FLOW = true;
    private static final int FLOW_COLOR = 0xB48CFF;

    /** Draw up to `amount` mana from batteries reachable from `origin`. Returns how much was drawn. */
    public static int draw(Level level, BlockPos origin, int amount) {
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();   // also serves as "visited"
        Deque<BlockPos> toVisit = new ArrayDeque<>();
        List<ManaBatteryBlockEntity> batteries = new ArrayList<>();
        cameFrom.put(origin, origin);
        toVisit.add(origin);
        int conduits = 0;

        while (!toVisit.isEmpty() && conduits <= MAX_CONDUITS) {
            BlockPos current = toVisit.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (cameFrom.containsKey(next) || !level.isLoaded(next)) continue;
                cameFrom.put(next, current);

                if (level.getBlockState(next).getBlock() instanceof ManaConduitBlock) {
                    toVisit.add(next);
                    conduits++;
                } else if (level.getBlockEntity(next) instanceof ManaBatteryBlockEntity battery && battery.getMana() > 0) {
                    batteries.add(battery);
                }
            }
        }

        int drawn = 0;
        for (ManaBatteryBlockEntity battery : batteries) {
            if (drawn >= amount) break;
            int taken = battery.extract(amount - drawn);
            drawn += taken;
            if (taken > 0 && SHOW_FLOW && level instanceof ServerLevel serverLevel) {
                SpellFx.flow(serverLevel, path(cameFrom, battery.getBlockPos(), origin), FLOW_COLOR, 0.10F);
            }
        }
        return drawn;
    }

    /** Block centres from the battery to the machine, keeping only the corners. */
    private static List<Vec3> path(Map<BlockPos, BlockPos> cameFrom, BlockPos battery, BlockPos origin) {
        List<BlockPos> steps = new ArrayList<>();
        BlockPos at = battery;
        while (at != null && !at.equals(origin) && steps.size() < MAX_CONDUITS + 2) {
            steps.add(at);
            at = cameFrom.get(at);
        }
        steps.add(origin);
        List<Vec3> corners = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            boolean end = i == 0 || i == steps.size() - 1;
            if (!end) {
                BlockPos before = steps.get(i - 1), here = steps.get(i), after = steps.get(i + 1);
                if (here.subtract(before).equals(after.subtract(here))) continue;   // still going straight
            }
            corners.add(Vec3.atCenterOf(steps.get(i)));
        }
        return corners;
    }
}
