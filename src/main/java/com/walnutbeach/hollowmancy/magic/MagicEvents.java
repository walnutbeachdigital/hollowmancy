package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.item.ArcanistGear;
import com.walnutbeach.hollowmancy.item.Artifacts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.patchouli.api.PatchouliAPI;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class MagicEvents {

    // ---- Keep the client's copy of magic data up to date ----

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MagicHelper.sync(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MagicHelper.sync(player);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MagicHelper.sync(player);
    }

    // ---- Mana regeneration: 2 per second once awakened ----

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return; // once per second

        MagicData data = MagicHelper.get(player);
        if (data.stage() < MagicStage.AWAKENING) return;
        if (data.mana() >= data.maxMana()) return;

        int regen = Math.max(HollowmancyConfig.MANA_REGEN.get(), data.maxMana() * HollowmancyConfig.MANA_REGEN.get() / 100);
        if (Artifacts.hasCrown(player)) regen *= 2;
        regen = ArcanistGear.regen(player, regen);
        MagicHelper.set(player, data.withMana(data.mana() + regen));
    }

    // ---- The Awakening: a shard held beneath a thunderstorm ----

    @SubscribeEvent
    public static void onAwakeningCheck(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return; // check once per second
        if (MagicHelper.get(player).stage() != MagicStage.MUNDANE) return;

        ServerLevel level = player.serverLevel();
        if (!level.isRaining()) return; // rain or thunder
        if (!level.canSeeSky(player.blockPosition())) return;

        boolean inMainHand = player.getMainHandItem().is(Hollowmancy.DULL_SHARD.get());
        boolean inOffHand = player.getOffhandItem().is(Hollowmancy.DULL_SHARD.get());
        if (!inMainHand && !inOffHand) return;

        float chance = level.isThundering() ? 0.10f : 0.03f; // storms are faster, rain still works
        if (player.getRandom().nextFloat() > chance) return;

        awaken(player, level, inMainHand ? player.getMainHandItem() : player.getOffhandItem());
    }

    private static void awaken(ServerPlayer player, ServerLevel level, ItemStack shard) {
        // Harmless, visual-only lightning
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(player.getX(), player.getY(), player.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        shard.shrink(1);

        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data.withStage(MagicStage.AWAKENING).withMana(data.maxMana()));

        player.sendSystemMessage(Component.translatable("hollowmancy.magic_events.the_shard_drinks_the_storm_and")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));

        // Hand the player their Arcane Journal
        ItemStack journal = PatchouliAPI.get().getBookStack(
                ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "arcane_journal"));
        if (!player.getInventory().add(journal)) {
            player.drop(journal, false);
        }
        ItemStack spellbook = new ItemStack(Hollowmancy.SPELLBOOK.get());
        if (!player.getInventory().add(spellbook)) {
            player.drop(spellbook, false);
        }
        player.sendSystemMessage(Component.translatable("hollowmancy.magic_events.a_worn_journal_has_appeared_in")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}