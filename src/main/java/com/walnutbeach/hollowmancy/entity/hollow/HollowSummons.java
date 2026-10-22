package com.walnutbeach.hollowmancy.entity.hollow;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Summoned servants. In the Hollow, where vanilla mobs are refused, casters call Hollow Shades;
 * anywhere else they call Vexes as before. Summoned Shades fade away when their time is up, like Vexes do.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class HollowSummons {
    private static final String TAG = "hollowmancy.summoned_shade";
    private static final String OWNER = "hollowmancy_summoner";
    private static final String EXPIRES = "hollowmancy_expires";

    public static boolean inHollow(ServerLevel level) {
        return level.dimension() == HollowSpawnRules.HOLLOW;
    }

    /** How many servants this caster has out within `range` blocks. */
    public static int count(ServerLevel level, Mob owner, double range) {
        int vexes = level.getEntitiesOfClass(Vex.class, owner.getBoundingBox().inflate(range), v -> v.getOwner() == owner).size();
        int shades = level.getEntitiesOfClass(HollowShade.class, owner.getBoundingBox().inflate(range),
                s -> s.getTags().contains(TAG) && s.getPersistentData().hasUUID(OWNER)
                        && s.getPersistentData().getUUID(OWNER).equals(owner.getUUID())).size();
        return vexes + shades;
    }

    /** Call one servant at (x, y, z) to fight `target` for `lifeTicks`. */
    public static Mob summon(ServerLevel level, Mob owner, LivingEntity target, double x, double y, double z, int lifeTicks) {
        if (!inHollow(level)) {
            Vex vex = EntityType.VEX.create(level);
            if (vex == null) return null;
            vex.moveTo(x, y, z, 0, 0);
            vex.setOwner(owner);
            vex.setLimitedLife(lifeTicks);
            vex.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            vex.setTarget(target);
            level.addFreshEntity(vex);
            return vex;
        }
        HollowShade shade = HollowCreatures.HOLLOW_SHADE.get().create(level);
        if (shade == null) return null;
        // Shades walk, so they rise from the ground under the point rather than appearing in the air
        double ground = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
        double sy = Math.abs(ground - y) < 4 ? ground : owner.getY();
        shade.moveTo(x, sy, z, level.random.nextFloat() * 360F, 0);
        shade.finalizeSpawn(level, level.getCurrentDifficultyAt(shade.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        shade.addTag(TAG);
        shade.getPersistentData().putUUID(OWNER, owner.getUUID());
        shade.getPersistentData().putLong(EXPIRES, level.getGameTime() + lifeTicks);
        shade.setTarget(target);
        level.addFreshEntity(shade);
        level.sendParticles(HollowmancyParticles.RING_VOID.get(), x, sy + 0.1, z, 1, 0, 0, 0, 0);
        level.sendParticles(HollowmancyParticles.VOID_WISP.get(), x, sy + 1, z, 16, 0.3, 0.7, 0.3, 0.04);
        return shade;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof HollowShade shade) || entity.tickCount % 20 != 0 || !(entity.level() instanceof ServerLevel level)) return;
        if (!shade.getTags().contains(TAG)) return;
        if (level.getGameTime() < shade.getPersistentData().getLong(EXPIRES)) return;
        level.sendParticles(HollowmancyParticles.VOID_WISP.get(), shade.getX(), shade.getY() + 1, shade.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
        shade.discard();
    }

    private HollowSummons() {}
}
