package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.StringJoiner;

public abstract class Spell {

    private final String id;
    private final String name;          // English; translations come from the language file
    private final String description;   // English
    private final MagicSchool school;
    private final int requiredStage;
    private final int knowledgeCost;   // 0 = known automatically once stage (and school) are reached
    private final int manaCost;
    private final int cooldownTicks;   // 20 ticks = 1 second
    private final ResourceLocation icon;

    /** General-school spell. */
    protected Spell(String id, String name, String description,
                    int requiredStage, int knowledgeCost, int manaCost, int cooldownTicks) {
        this(id, name, description, MagicSchool.GENERAL, requiredStage, knowledgeCost, manaCost, cooldownTicks);
    }

    protected Spell(String id, String name, String description, MagicSchool school,
                    int requiredStage, int knowledgeCost, int manaCost, int cooldownTicks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.school = school;
        this.requiredStage = requiredStage;
        this.knowledgeCost = knowledgeCost;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.icon = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "textures/gui/spell/" + id + ".png");
    }

    public String getId() { return id; }
    public MagicSchool getSchool() { return school; }
    public int getRequiredStage() { return requiredStage; }
    public int getKnowledgeCost() { return knowledgeCost; }
    public int getManaCost() { return manaCost; }
    public int getCooldownTicks() { return cooldownTicks; }
    public ResourceLocation getIcon() { return icon; }

    // ---------------- Names and text (translatable) ----------------

    /** Language key for the name, e.g. hollowmancy.spell.firebolt (the description adds ".desc"). */
    public String nameKey() { return "hollowmancy.spell." + id; }

    /** The spell's name in the player's language (on the client; English on a dedicated server). */
    public String getName() {
        return Language.getInstance().getOrDefault(nameKey(), name);
    }

    public String getEnglishName() { return name; }

    /** For chat messages sent from the server: each player sees it in their own language. */
    public MutableComponent nameComponent() {
        return Component.translatableWithFallback(nameKey(), name);
    }

    public String getDescription() {
        return Language.getInstance().getOrDefault(nameKey() + ".desc", description);
    }

    /**
     * What tier 2 or 3 adds, in the player's language. Spells write their English text in
     * getUpgradeDescription; the language key is made from that text, e.g.
     * hollowmancy.spell.firebolt.upgrade.bolts_split_into_three, so it can never point at the wrong tier.
     */
    public String upgradeText(int tier) {
        String english = getUpgradeDescription(tier);
        if (english == null || english.isEmpty()) return english;
        return Language.getInstance().getOrDefault(upgradeKey(english), english);
    }

    public String upgradeKey(String english) {
        return nameKey() + ".upgrade." + slug(english);
    }

    /** First ten words, lower case, joined with _ (the installer builds the same keys). */
    public static String slug(String text) {
        StringJoiner joined = new StringJoiner("_");
        int words = 0;
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.isEmpty()) continue;
            joined.add(word);
            if (++words == 10) break;
        }
        return joined.toString();
    }

    /** Corruption gained each time this spell is cast. Forbidden spells override this. */
    public int getCorruption() {
        return 0;
    }

    /** What tier 2 or 3 adds, in English. Shown through upgradeText(tier). */
    public String getUpgradeDescription(int tier) {
        return "";
    }

    /** This player's tier (1-4) in this spell. */
    protected int tier(ServerPlayer player) {
        return Spells.getTier(MagicHelper.get(player), this);
    }

    /** A line of particles from one point to another. */
    protected static void drawLine(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to, double step) {
        Vec3 path = to.subtract(from);
        double length = path.length();
        if (length < 0.01) return;
        Vec3 dir = path.normalize();
        for (double d = 0; d < length; d += step) {
            Vec3 point = from.add(dir.scale(d));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    /** How long the caster must channel before the spell fires, in ticks. 0 = instant. */
    public int getChargeTicks() {
        return 0;
    }

    /** Whether taking damage while channeling breaks this spell. */
    public boolean isInterruptedByDamage() {
        return false;
    }

    /** Perform the spell. Return false if it fizzled (no mana is spent, no cooldown starts). */
    public abstract boolean cast(ServerPlayer player);
}
