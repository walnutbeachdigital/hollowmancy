package com.walnutbeach.hollowmancy.ending;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** What happens when the Listener falls: the title, Archmage, and (the first time) the credits. */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class Ending {

    public static final int ARCHMAGE_MANA = 100;

    private record Pending(UUID player, int ticks) {}
    private static final List<Pending> CREDITS = new ArrayList<>();

    public static void begin(ServerLevel level, Vec3 at) {
        AdvancementHolder archmage = level.getServer().getAdvancements()
            .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "archmage"));
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new net.minecraft.world.phys.AABB(at, at).inflate(128))) {
            boolean firstTime = archmage == null || !player.getAdvancements().getOrStartProgress(archmage).isDone();
            if (archmage != null) player.getAdvancements().award(archmage, "unmade");

            player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 100, 40));
            player.connection.send(new ClientboundSetSubtitleTextPacket((firstTime ? Component.translatable("hollowmancy.ending.you_are_an_archmage") : Component.translatable("hollowmancy.ending.again"))
                .withStyle(ChatFormatting.GOLD)));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("hollowmancy.ending.the_hollow_is_silent")
                .withStyle(ChatFormatting.DARK_PURPLE)));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 0.8f);
            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 0.6f, 0.8f);

            if (!firstTime) continue;
            MagicData data = MagicHelper.get(player);
            MagicData promoted = data.withMaxMana(data.maxMana() + ARCHMAGE_MANA);
            if (data.stage() < MagicStage.ARCHMAGE) promoted = promoted.withStage(MagicStage.ARCHMAGE);
            MagicHelper.set(player, promoted.withMana(promoted.maxMana()).withCorruption(0));
            player.sendSystemMessage(Component.translatable("hollowmancy.ending.the_thing_that_listened_is_gone", ARCHMAGE_MANA)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            CREDITS.add(new Pending(player.getUUID(), 160));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (CREDITS.isEmpty()) return;
        List<Pending> due = new ArrayList<>();
        List<Pending> waiting = new ArrayList<>();
        for (Pending p : CREDITS) (p.ticks() <= 1 ? due : waiting).add(new Pending(p.player(), p.ticks() - 1));
        CREDITS.clear();
        CREDITS.addAll(waiting);
        for (Pending p : due) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(p.player());
            if (player != null) PacketDistributor.sendToPlayer(player, CreditsPayload.INSTANCE);
        }
    }
}
