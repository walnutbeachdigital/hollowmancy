package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

public class ManaBeaconBlockEntity extends BlockEntity {

    public static final int RADIUS = 24;
    public static final int BUFFER = 200;
    public static final int PULSE_INTERVAL = 80;   // every 4 seconds
    public static final int COST_PER_PLAYER = 3;
    public static final int WELLSPRING_GIFT = 2;   // mana given per pulse (less than it costs, so no free mana)

    /** What the beacon can do, and the item that tunes it. effect == null means Wellspring (mana). */
    public record Blessing(String id, String name, Item key, Holder<MobEffect> effect) {}

    public static final List<Blessing> BLESSINGS = List.of(
        new Blessing("speed", "Swiftness", Items.SUGAR, MobEffects.MOVEMENT_SPEED),
        new Blessing("haste", "Haste", Items.GOLD_INGOT, MobEffects.DIG_SPEED),
        new Blessing("resistance", "Resistance", Items.IRON_INGOT, MobEffects.DAMAGE_RESISTANCE),
        new Blessing("jump", "Leaping", Items.RABBIT_FOOT, MobEffects.JUMP),
        new Blessing("night_vision", "Night Vision", Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION),
        new Blessing("regeneration", "Regeneration", Items.GHAST_TEAR, MobEffects.REGENERATION),
        new Blessing("wellspring", "Wellspring", Items.AMETHYST_SHARD, null)
    );

    public static Blessing forItem(ItemStack stack) {
        for (Blessing blessing : BLESSINGS) {
            if (stack.is(blessing.key())) return blessing;
        }
        return null;
    }

    private static Blessing byId(String id) {
        for (Blessing blessing : BLESSINGS) {
            if (blessing.id().equals(id)) return blessing;
        }
        return null;
    }

    private int mana = 0;
    private int ticks = 0;
    private String blessingId = "";

    public ManaBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_BEACON.get(), pos, state);
    }

    public int getMana() {
        return mana;
    }

    public Blessing getBlessing() {
        return byId(blessingId);
    }

    public void setBlessing(Blessing blessing) {
        blessingId = blessing.id();
        setChanged();
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ticks++;

        if (ticks % 20 == 0 && mana < BUFFER / 2) {
            int drawn = ManaNetwork.draw(level, worldPosition, BUFFER - mana);
            if (drawn > 0) {
                mana += drawn;
                setChanged();
            }
        }

        Blessing blessing = getBlessing();
        boolean active = blessing != null && mana >= COST_PER_PLAYER;
        setActive(active);
        if (!active) return;

        // A stream of light rising from the top
        if (ticks % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.05,
                worldPosition.getZ() + 0.5, 0, 0.0, 1.0, 0.0, 0.3);
        }
        if (ticks % PULSE_INTERVAL != 0) return;

        boolean spent = false;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(HollowmancyConfig.BEACON_RADIUS.get()))) {
            if (mana < COST_PER_PLAYER) break;
            if (player.isSpectator()) continue;
            if (blessing.effect() != null) {
                player.addEffect(new MobEffectInstance(blessing.effect(), PULSE_INTERVAL + 60, 0, true, true));
            } else {
                MagicData data = MagicHelper.get(player);
                if (data.stage() < MagicStage.AWAKENING || data.mana() >= data.maxMana()) continue;
                MagicHelper.set(player, data.withMana(data.mana() + WELLSPRING_GIFT));
            }
            mana -= COST_PER_PLAYER;
            spent = true;
        }
        if (spent) setChanged();
    }

    private void setActive(boolean active) {
        BlockState state = getBlockState();
        if (state.getValue(ManaEngineBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(ManaEngineBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mana", mana);
        tag.putString("blessing", blessingId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = tag.getInt("mana");
        blessingId = tag.getString("blessing");
    }
}
