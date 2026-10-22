package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class StageAdvancements {

    private static final String CRITERION = "reached";

    // Index = stage number. Stage 1 (Mundane) has no advancement.
    private static final String[] ADVANCEMENT_IDS = {
            null,          // 0 (unused)
            null,          // 1 The Mundane
            "awakening",   // 2
            "apprentice",  // 3
            "arcane_arts", // 4
            "mastery",     // 5
            "ascension"    // 6
    };

    /** Grant every stage advancement at or below the player's stage; revoke the ones above it. */
    public static void update(ServerPlayer player) {
        int stage = MagicHelper.get(player).stage();

        for (int s = MagicStage.AWAKENING; s <= MagicStage.ASCENSION; s++) {
            AdvancementHolder advancement = player.server.getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, ADVANCEMENT_IDS[s]));
            if (advancement == null) continue;

            if (stage >= s) {
                player.getAdvancements().award(advancement, CRITERION);
            } else {
                player.getAdvancements().revoke(advancement, CRITERION);
            }
        }
    }

    // Catch existing worlds up when a player joins
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) update(player);
    }
}