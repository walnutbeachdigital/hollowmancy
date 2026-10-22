package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.network.SyncMagicPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class MagicHelper {

    public static MagicData get(Player player) {
        return player.getData(ModAttachments.MAGIC);
    }

    public static void set(ServerPlayer player, MagicData data) {
        int oldStage = get(player).stage();
        player.setData(ModAttachments.MAGIC, data);
        sync(player);

        if (data.stage() != oldStage) {
            StageAdvancements.update(player);
        }
    }

    public static boolean hasStage(Player player, int stage) {
        return get(player).stage() >= stage;
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncMagicPayload(get(player)));
    }
}