package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class StormcallSpell extends Spell {

    private static final double RANGE = 32.0;
    private static final int[][] EXTRA_OFFSETS = {{2, 2}, {-2, -2}};

    public StormcallSpell() {
        super("stormcall", "Stormcall", "Call down true lightning wherever you are looking, up to 32 blocks away. It needs open sky.",
                MagicSchool.ELEMENTAL, MagicStage.MASTERY, 7, 60, 300);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Two bolts strike around your target." : "Three bolts strike around your target.";
    }

    @Override
    public int getChargeTicks() {
        return 30; // 1.5 seconds
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 hitPos = blockHit.getLocation();

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, hitPos, searchBox,
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator());
        if (entityHit != null) hitPos = entityHit.getLocation();

        BlockPos strikePos = BlockPos.containing(hitPos);
        if (!level.canSeeSky(strikePos.above())) {
            player.displayClientMessage(Component.translatable("hollowmancy.stormcall_spell.the_sky_cannot_reach_there")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        List<BlockPos> spots = new ArrayList<>();
        spots.add(strikePos);
        for (int i = 1; i < tier; i++) {
            BlockPos offset = strikePos.offset(EXTRA_OFFSETS[i - 1][0], 0, EXTRA_OFFSETS[i - 1][1]);
            spots.add(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, offset));
        }

        for (BlockPos spot : spots) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) continue;
            bolt.moveTo(Vec3.atBottomCenterOf(spot));
            bolt.setCause(player);
            level.addFreshEntity(bolt);
            SpellFx.ring(level, HollowmancyParticles.RING_STORM.get(), Vec3.atBottomCenterOf(spot).add(0, 0.1, 0), 3.0F);
            level.sendParticles(HollowmancyParticles.STORM_SPARK.get(), spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 30, 0.6, 0.6, 0.6, 0.2);
        }
        return true;
    }
}