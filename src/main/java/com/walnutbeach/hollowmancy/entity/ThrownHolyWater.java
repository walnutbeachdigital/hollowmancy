package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.HolyWaterItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownHolyWater extends ThrowableItemProjectile {

    public ThrownHolyWater(EntityType<? extends ThrownHolyWater> type, Level level) {
        super(type, level);
    }

    public ThrownHolyWater(Level level, LivingEntity thrower) {
        super(ModEntities.HOLY_WATER.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Hollowmancy.SPLASH_HOLY_WATER.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            HolyWaterItem.splash(level, position(), getOwner());
            level.levelEvent(2002, blockPosition(), 0xBFE8FF);   // vanilla splash: glass break + pale blue droplets
            discard();
        }
    }
}
