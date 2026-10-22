package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.walnutbeach.hollowmancy.archmage.ArchmageItems;
import com.walnutbeach.hollowmancy.archmage.ArchmageStaffItem;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Artifacts {

    public static final float WAND_DISCOUNT = 0.25f;
    public static final float ABYSSAL_DISCOUNT = 0.35f;
    public static final float AMULET_ABSORB = 0.30f;
    public static final int AMULET_MANA_PER_POINT = 5;

    /** Spell mana cost after artifact and gear bonuses. */
    public static int effectiveManaCost(ServerPlayer player, int baseCost) {
        int cost = baseCost;
        ItemStack held = player.getMainHandItem();
        if (held.is(Hollowmancy.ABYSSAL_FOCUS.get())) {
            cost = Math.max(1, Math.round(cost * (1.0f - ABYSSAL_DISCOUNT)));
        } else if (held.is(ArchmageItems.ARCHMAGE_STAFF.get())) {
            cost = Math.max(1, Math.round(cost * (1.0f - ArchmageStaffItem.DISCOUNT)));
        } else if (held.is(Hollowmancy.FOCUS_WAND.get()) || held.is(Hollowmancy.WELLSPRING_WAND.get())) {
            cost = Math.max(1, Math.round(cost * (1.0f - WAND_DISCOUNT)));
        }
        return ArcanistGear.manaCost(player, WandMastery.cost(player, cost));
    }

    /** Corruption gained from a cast after artifact and gear bonuses. */
    public static int effectiveCorruption(ServerPlayer player, int baseCorruption) {
        int corruption = baseCorruption;
        if (corruption > 0 && (player.getMainHandItem().is(Hollowmancy.ABYSSAL_FOCUS.get()) || player.getMainHandItem().is(ArchmageItems.ARCHMAGE_STAFF.get()))) {
            corruption = (corruption + 1) / 2; // half, rounded up
        }
        return ArcanistGear.corruption(player, corruption);
    }

    /** The Wellspring Wand in the main hand, or EMPTY. */
    private static ItemStack heldWellspring(net.minecraft.world.entity.player.Player player) {
        ItemStack held = player.getMainHandItem();
        return held.is(Hollowmancy.WELLSPRING_WAND.get()) ? held : ItemStack.EMPTY;
    }

    /** Your mana plus whatever a held Wellspring Wand holds. */
    public static int availableMana(ServerPlayer player) {
        return MagicHelper.get(player).mana() + ManaVessels.stored(heldWellspring(player));
    }

    /** Take as much of `cost` as possible from a held Wellspring Wand. Returns what's left for the player to pay. */
    public static int drawFromWand(ServerPlayer player, int cost) {
        ItemStack wand = heldWellspring(player);
        if (wand.isEmpty()) return cost;
        int stored = ManaVessels.stored(wand);
        int taken = Math.min(stored, cost);
        if (taken > 0) ManaVessels.setStored(wand, stored - taken);
        return cost - taken;
    }

    /** The Resonant Prism in the other hand: spells recharge 25% faster. */
    public static int prismCooldown(net.minecraft.world.entity.player.Player player, int ticks) {
        ticks = WandMastery.cooldown(player, ticks);
        if (player.getMainHandItem().is(ArchmageItems.ARCHMAGE_STAFF.get())) ticks = Math.max(1, Math.round(ticks * ArchmageStaffItem.COOLDOWN));
        return player.getOffhandItem().is(Hollowmancy.RESONANT_PRISM.get()) ? Math.max(1, Math.round(ticks * 0.75f)) : ticks;
    }

    /** The Warding Amulet anywhere in the inventory, or the Warded Robe worn. */
    public static boolean hasWard(net.minecraft.world.entity.player.Player player) {
        return player.getInventory().hasAnyMatching(stack -> stack.is(Hollowmancy.WARDING_AMULET.get()))
                || player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(Hollowmancy.WARDED_ROBE.get());
    }

    /** The Sovereign's Crown anywhere in the inventory, or the Crowned Hood worn. */
    public static boolean hasCrown(net.minecraft.world.entity.player.Player player) {
        return player.getInventory().hasAnyMatching(stack -> stack.is(Hollowmancy.SOVEREIGN_CROWN.get()))
                || player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Hollowmancy.CROWNED_HOOD.get());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasWard(player)) return;

        float absorbed = event.getAmount() * AMULET_ABSORB;
        int manaCost = Math.max(1, Math.round(absorbed * AMULET_MANA_PER_POINT));

        MagicData data = MagicHelper.get(player);
        if (data.mana() < manaCost) return;

        event.setAmount(event.getAmount() - absorbed);
        MagicHelper.set(player, data.withMana(data.mana() - manaCost));

        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.4, 0.5, 0.4, 0.02);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8f, 1.5f);
    }
}