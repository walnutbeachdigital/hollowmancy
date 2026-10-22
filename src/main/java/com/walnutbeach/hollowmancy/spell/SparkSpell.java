package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class SparkSpell extends Spell {

    private static final double RANGE = 16.0;
    private static final double CHAIN_RANGE = 6.0;

    public SparkSpell() {
        super("spark", "Spark", "A crackling bolt that strikes the first creature in its path.",
                MagicStage.AWAKENING, 0, 10, 20);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Arcs to a second nearby creature. 2.5 hearts per hit."
                : "Arcs to two more creatures. 3 hearts per hit.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        float damage = tier == 1 ? 4.0f : tier == 2 ? 5.0f : 6.0f;
        int chains = tier - 1;

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

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity first) {
            hitPos = entityHit.getLocation();
            first.hurt(player.damageSources().indirectMagic(player, player), Combos.shatter(player, first, damage));

            // Chain to nearby creatures (never to other players)
            List<LivingEntity> struck = new ArrayList<>();
            struck.add(first);
            LivingEntity current = first;
            for (int i = 0; i < chains; i++) {
                LivingEntity source = current;
                LivingEntity next = level.getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(CHAIN_RANGE),
                                entity -> entity != player && entity.isAlive() && !(entity instanceof Player) && !struck.contains(entity))
                        .stream()
                        .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(source)))
                        .orElse(null);
                if (next == null) break;

                SpellFx.beam(level, source.position().add(0, source.getBbHeight() / 2, 0),
                        next.position().add(0, next.getBbHeight() / 2, 0), SpellFx.STORM, 0.10F, 8, SpellFx.LIGHTNING);
                next.hurt(player.damageSources().indirectMagic(player, player), Combos.shatter(player, next, damage));
                struck.add(next);
                current = next;
            }
        }

        SpellFx.beam(level, start.add(look).add(0, -0.2, 0), hitPos, SpellFx.STORM, 0.12F, 8, SpellFx.LIGHTNING);
        level.sendParticles(HollowmancyParticles.STORM_SPARK.get(), hitPos.x, hitPos.y, hitPos.z, 20, 0.2, 0.2, 0.2, 0.15);
        level.sendParticles(HollowmancyParticles.ARCANE_SPARK.get(), hitPos.x, hitPos.y, hitPos.z, 8, 0.2, 0.2, 0.2, 0.05);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.6f);
        level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.8f);
        return true;
    }
}