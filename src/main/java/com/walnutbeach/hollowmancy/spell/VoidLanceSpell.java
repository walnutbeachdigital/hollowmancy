package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class VoidLanceSpell extends Spell {

    public VoidLanceSpell() {
        super("void_lance", "Void Lance", "A spear of nothing. Deals 6 hearts to the first creature it pierces. (+6 corruption)",
                MagicSchool.FORBIDDEN, MagicStage.ASCENSION, 5, 40, 40);
    }

    @Override
    public int getCorruption() {
        return 6;
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "7.5 hearts, with a 32-block reach."
                : "9 hearts, a 40-block reach, and it pierces every creature in its path.";
    }

    @Override
    public int getChargeTicks() {
        return 20; // 1 second
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        double range = tier == 1 ? 24.0 : tier == 2 ? 32.0 : 40.0;
        float damage = tier == 1 ? 12.0f : tier == 2 ? 15.0f : 18.0f;

        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 wallPos = blockHit.getLocation();
        Vec3 hitPos = wallPos;
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);

        List<LivingEntity> hits = new ArrayList<>();
        if (tier >= 3) {
            // Pierce: everything the beam passes through, up to the wall
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {
                if (target.getBoundingBox().inflate(0.3).clip(start, wallPos).isPresent()) hits.add(target);
            }
        } else {
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, wallPos, searchBox,
                    entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator());
            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
                hits.add(target);
                hitPos = entityHit.getLocation();
            }
        }

        for (LivingEntity target : hits) {
            target.hurt(player.damageSources().indirectMagic(player, player), Combos.shatter(player, target, damage));
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
        }

        SpellFx.beam(level, start.add(look).add(0, -0.2, 0), hitPos, SpellFx.VOID, 0.22F, 14, SpellFx.SMOOTH);
        drawLine(level, HollowmancyParticles.VOID_WISP.get(), start.add(look), hitPos, 1.2);
        level.sendParticles(HollowmancyParticles.VOID_WISP.get(), hitPos.x, hitPos.y, hitPos.z, 15, 0.3, 0.3, 0.3, 0.05);
        SpellFx.runeCircle(level, HollowmancyParticles.RUNE_VOID.get(), hitPos, 0.6, 3);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6f, 1.4f);
        return true;
    }
}