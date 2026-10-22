package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.magic.HollowmancyAdvancements;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

/**
 * Keeps each player's familiar with them. The bond for every familiar type lives on the player,
 * so dismissing, dying, logging out or switching familiars never loses progress.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Familiars {

    private static final String ACTIVE = "hollowmancy_familiar";
    private static final String ENTITY = "hollowmancy_familiar_uuid";
    private static final String BOND = "hollowmancy_bond_";

    // ---------------- Stored on the player ----------------

    public static int loadBond(Player player, FamiliarType type) {
        return player.getPersistentData().getInt(BOND + type.id());
    }

    public static void saveBond(Player player, FamiliarType type, int bond) {
        player.getPersistentData().putInt(BOND + type.id(), bond);
    }

    public static FamiliarType activeType(Player player) {
        return FamiliarType.byId(player.getPersistentData().getString(ACTIVE));
    }

    /** The player's familiar, wherever it is loaded, or null. */
    public static FamiliarEntity find(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(ENTITY)) return null;
        UUID id = data.getUUID(ENTITY);
        if (player.getServer() == null) return null;
        for (ServerLevel level : player.getServer().getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof FamiliarEntity familiar && familiar.isAlive()) return familiar;
        }
        return null;
    }

    // ---------------- Summoning ----------------

    public static FamiliarEntity summon(ServerPlayer player, FamiliarType type, Vec3 at) {
        removeCurrent(player);
        ServerLevel level = player.serverLevel();
        FamiliarEntity familiar = type.entityType().create(level);
        if (familiar == null) return null;
        familiar.setTame(true, false);
        familiar.setOwnerUUID(player.getUUID());
        familiar.setBond(loadBond(player, type));
        familiar.moveTo(at.x, at.y, at.z, player.getYRot(), 0.0f);
        level.addFreshEntity(familiar);

        player.getPersistentData().putString(ACTIVE, type.id());
        player.getPersistentData().putUUID(ENTITY, familiar.getUUID());
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.4, at.z, 20, 0.3, 0.3, 0.3, 0.05);
        HollowmancyAdvancements.award(player, "first_familiar", "bound");
        HollowmancyAdvancements.award(player, "familiar_" + type.id(), "bound");
        HollowmancyAdvancements.award(player, "all_familiars", type.id());
        return familiar;
    }

    public static void dismiss(ServerPlayer player) {
        FamiliarEntity familiar = find(player);
        if (familiar != null && familiar.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, familiar.getX(), familiar.getY() + 0.3, familiar.getZ(), 12, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, familiar.getX(), familiar.getY(), familiar.getZ(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 1.0f, 0.8f);
        }
        removeCurrent(player);
        player.getPersistentData().remove(ACTIVE);
    }

    private static void removeCurrent(ServerPlayer player) {
        FamiliarEntity familiar = find(player);
        if (familiar != null) familiar.discard();
        player.getPersistentData().remove(ENTITY);
    }

    /** Bring the active familiar to the player (after login, respawn, travel, or if it got lost). */
    public static void resummon(ServerPlayer player) {
        FamiliarType type = activeType(player);
        if (type == null) return;
        summon(player, type, player.position().add(0.6, 1.0, 0.6));
    }

    // ---------------- Keeping it with its owner ----------------

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) resummon(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) removeCurrent(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) resummon(player);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) resummon(player);
    }

    // The player's saved data doesn't survive death on its own: carry it over
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag from = event.getOriginal().getPersistentData();
        CompoundTag to = event.getEntity().getPersistentData();
        for (String key : from.getAllKeys()) {
            if (key.startsWith(BOND) || key.equals(ACTIVE) || key.equals(ENTITY)) to.put(key, from.get(key).copy());
        }
    }

    // Every 5 seconds: if the familiar was lost (unloaded, left behind), bring it back
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 100 != 0) return;
        if (activeType(player) == null || player.isSpectator()) return;
        FamiliarEntity familiar = find(player);
        if (familiar == null || familiar.level() != player.level() || familiar.distanceToSqr(player) > 96 * 96) {
            resummon(player);
        }
    }

    // Fighting together deepens the bond
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;
        Entity killer = event.getSource().getEntity();
        ServerPlayer owner = killer instanceof ServerPlayer player ? player
            : killer instanceof FamiliarEntity familiar && familiar.getOwner() instanceof ServerPlayer player ? player : null;
        if (owner == null) return;
        FamiliarEntity familiar = find(owner);
        if (familiar != null && familiar.distanceToSqr(owner) < 32 * 32) familiar.addBond(HollowmancyConfig.BOND_PER_KILL.get());
    }
}
