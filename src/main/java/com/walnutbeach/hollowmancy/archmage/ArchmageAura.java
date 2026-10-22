package com.walnutbeach.hollowmancy.archmage;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.HollowmancyExtraConfig;
import com.walnutbeach.hollowmancy.fx.SpellFx;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The Archmage's aura: motes of your school's colour circle you, and a glyph turns under your feet now and then.
 * The colour follows the school of the last spell you cast. "/hollowmancy aura" turns it on or off for you;
 * the "archmageAura" setting turns it off for everyone.
 *
 * Also hands every Archmage their first Silent Seal (once), including players who became Archmage
 * before this update.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class ArchmageAura {
    private static final String KEY = "hollowmancy_archmage";
    private static final String OFF = "aura_off";
    private static final String SCHOOL = "aura_school";
    private static final String SEAL_GIVEN = "silent_seal_given";

    private static CompoundTag data(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) root.put(KEY, new CompoundTag());
        return root.getCompound(KEY);
    }

    public static void remember(ServerPlayer player, MagicSchool school) {
        data(player).putString(SCHOOL, school.getId());
    }

    private static MagicSchool school(ServerPlayer player) {
        String id = data(player).getString(SCHOOL);
        for (MagicSchool school : MagicSchool.values()) if (school.getId().equals(id)) return school;
        return MagicSchool.GENERAL;
    }

    public static boolean enabled(ServerPlayer player) {
        return !data(player).getBoolean(OFF);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 100 == 0) giveFirstSeal(player);
        if (player.tickCount % 4 != 0 || player.isSpectator() || player.isInvisible()) return;
        if (!HollowmancyExtraConfig.get(HollowmancyExtraConfig.ARCHMAGE_AURA) || !enabled(player)) return;
        if (MagicHelper.get(player).stage() < MagicStage.ARCHMAGE) return;

        ServerLevel level = player.serverLevel();
        MagicSchool school = school(player);
        ParticleOptions mote = SpellFx.spark(school);
        double t = player.tickCount * 0.09;
        for (int i = 0; i < 2; i++) {
            double a = t + i * Math.PI;
            double y = 0.3 + (Math.sin(t * 0.7 + i) + 1) * 0.6;
            level.sendParticles(mote, player.getX() + Math.cos(a) * 0.75, player.getY() + y, player.getZ() + Math.sin(a) * 0.75, 1, 0, 0, 0, 0);
        }
        if (player.tickCount % 60 == 0 && player.onGround()) {
            level.sendParticles(SpellFx.rune(school), player.getX(), player.getY() + 0.05, player.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private static void giveFirstSeal(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (tag.getBoolean(SEAL_GIVEN)) return;
        AdvancementHolder archmage = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "archmage"));
        if (archmage == null || !player.getAdvancements().getOrStartProgress(archmage).isDone()) return;
        tag.putBoolean(SEAL_GIVEN, true);
        ItemStack seal = new ItemStack(ArchmageItems.SILENT_SEAL.get());
        if (!player.getInventory().add(seal)) player.drop(seal, false);
        player.sendSystemMessage(Component.translatable("hollowmancy.silent_seal.given").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    /** Keep the aura settings (and the "seal given" mark) through death and returning from the End. */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData();
        if (old.contains(KEY)) event.getEntity().getPersistentData().put(KEY, old.getCompound(KEY).copy());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Hollowmancy.MODID)
                .then(Commands.literal("aura")
                        .executes(ctx -> set(ctx.getSource(), !enabled(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("on", BoolArgumentType.bool())
                                .executes(ctx -> set(ctx.getSource(), BoolArgumentType.getBool(ctx, "on"))))));
    }

    private static int set(CommandSourceStack source, boolean on) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        data(player).putBoolean(OFF, !on);
        String key = MagicHelper.get(player).stage() < MagicStage.ARCHMAGE ? "hollowmancy.aura.not_yet"
                : on ? "hollowmancy.aura.on" : "hollowmancy.aura.off";
        source.sendSuccess(() -> Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private ArchmageAura() {}
}
