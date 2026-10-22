package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public enum MagicSchool {

    GENERAL("general", "General", 0xFFFFFF, MagicStage.AWAKENING, 0),
    ELEMENTAL("elemental", "Elemental", 0xFF7A3D, MagicStage.ARCANE_ARTS, 5),
    WARDING("warding", "Warding", 0x5DA9E9, MagicStage.ARCANE_ARTS, 5),
    DIVINATION("divination", "Divination", 0xE9C46A, MagicStage.ARCANE_ARTS, 5),
    NATURE("nature", "Nature", 0x6BBF59, MagicStage.ARCANE_ARTS, 5),
    FORBIDDEN("forbidden", "Forbidden", 0xB0263F, MagicStage.ASCENSION, 10);

    public static final String PREFIX = "school:";

    private final String id;
    private final String englishName;
    private final int color;
    private final int requiredStage;
    private final int knowledgeCost;

    MagicSchool(String id, String englishName, int color, int requiredStage, int knowledgeCost) {
        this.id = id;
        this.englishName = englishName;
        this.color = color;
        this.requiredStage = requiredStage;
        this.knowledgeCost = knowledgeCost;
    }

    public String getId() { return id; }
    public int getColor() { return color; }
    public int getRequiredStage() { return requiredStage; }
    public int getKnowledgeCost() { return knowledgeCost; }

    /** Language key, e.g. hollowmancy.school.elemental */
    public String nameKey() { return "hollowmancy.school." + id; }

    /** The school's name in the player's language (on the client; English on a dedicated server). */
    public String getDisplayName() {
        return Language.getInstance().getOrDefault(nameKey(), englishName);
    }

    /** For chat messages sent from the server: each player sees it in their own language. */
    public MutableComponent displayComponent() {
        return Component.translatableWithFallback(nameKey(), englishName);
    }

    /** The entry stored in MagicData.learned when this school is unlocked, e.g. "school:elemental". */
    public String unlockId() {
        return PREFIX + id;
    }

    public boolean isUnlocked(MagicData data) {
        return this == GENERAL || data.learned().contains(unlockId());
    }

    public static MagicSchool fromUnlockId(String unlockId) {
        for (MagicSchool school : values()) {
            if (school.unlockId().equals(unlockId)) return school;
        }
        return null;
    }

    /** Server side: spend Knowledge to open a school. */
    public static void tryUnlock(ServerPlayer player, String unlockId) {
        MagicSchool school = fromUnlockId(unlockId);
        if (school == null || school == GENERAL) return;

        MagicData data = MagicHelper.get(player);
        if (school.isUnlocked(data)) return;
        if (data.stage() < school.getRequiredStage()) return;
        if (data.knowledge() < school.getKnowledgeCost()) return;

        MagicHelper.set(player, data
                .withKnowledge(data.knowledge() - school.getKnowledgeCost())
                .withLearned(school.unlockId()));

        player.sendSystemMessage(Component.translatable("hollowmancy.magic_school.you_have_begun_the_study_of", school.displayComponent())
                .withStyle(style -> style.withColor(TextColor.fromRgb(school.getColor())).withItalic(true)));
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
