package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Grants Hollowmancy's advancements from code (rituals, familiars, wand mastery, legendary spells). */
public final class HollowmancyAdvancements {
    public static void award(ServerPlayer player, String id, String criterion) {
        AdvancementHolder advancement = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, id));
        if (advancement != null) player.getAdvancements().award(advancement, criterion);
    }

    private HollowmancyAdvancements() {}
}
