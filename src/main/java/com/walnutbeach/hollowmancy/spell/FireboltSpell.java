package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.fx.SpellFx;

public class FireboltSpell extends Spell {

    private static final float SPREAD_DEGREES = 10.0f;

    public FireboltSpell() {
        super("firebolt", "Firebolt", "Hurl a bolt of flame that ignites whatever it strikes.",
                MagicSchool.ELEMENTAL, MagicStage.ARCANE_ARTS, 4, 20, 15);
    }

    @Override
    public String getUpgradeDescription(int tier) {
        return tier == 2 ? "Hurls three bolts in a fan." : "Hurls five bolts in a fan.";
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int tier = tier(player);
        int count = tier == 1 ? 1 : tier == 2 ? 3 : 5;

        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();

        for (int i = 0; i < count; i++) {
            float angle = (i - (count - 1) / 2.0f) * SPREAD_DEGREES;
            Vec3 dir = look.yRot((float) Math.toRadians(angle));

            SmallFireball fireball = new SmallFireball(level, player, dir);
            fireball.setPos(player.getX() + dir.x, player.getEyeY() - 0.1 + dir.y, player.getZ() + dir.z);
            level.addFreshEntity(fireball);
            SpellFx.trail(fireball, HollowmancyParticles.EMBER.get(), HollowmancyParticles.RUNE_FIRE.get());
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
        return true;
    }
}