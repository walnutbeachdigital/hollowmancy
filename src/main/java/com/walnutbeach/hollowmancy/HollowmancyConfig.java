package com.walnutbeach.hollowmancy;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Hollowmancy's settings. A server config: each world has its own copy, at
 * saves/<world>/serverconfig/hollowmancy-server.toml, and servers send theirs to players.
 * Values are read while the game is running, so changes apply on the next world load.
 */
public class HollowmancyConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ---- Mana and spells ----
    public static final ModConfigSpec.IntValue MANA_REGEN;
    public static final ModConfigSpec.DoubleValue SPELL_COST;
    public static final ModConfigSpec.DoubleValue SPELL_COOLDOWN;
    // ---- Corruption ----
    public static final ModConfigSpec.DoubleValue CORRUPTION_GAIN;
    public static final ModConfigSpec.IntValue CORRUPTION_DECAY_SECONDS;
    public static final ModConfigSpec.IntValue HOLY_WATER_CLEANSE;
    // ---- Bosses ----
    public static final ModConfigSpec.DoubleValue SOVEREIGN_HEALTH;
    public static final ModConfigSpec.DoubleValue COLOSSUS_HEALTH;
    public static final ModConfigSpec.DoubleValue KING_HEALTH;
    public static final ModConfigSpec.DoubleValue WIZARD_HEALTH;
    public static final ModConfigSpec.DoubleValue CASTELLAN_HEALTH;
    public static final ModConfigSpec.DoubleValue LEVIATHAN_HEALTH;
    public static final ModConfigSpec.DoubleValue LISTENER_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_DAMAGE;
    // ---- The world ----
    public static final ModConfigSpec.DoubleValue LEY_RITUAL_DISCOUNT;
    public static final ModConfigSpec.IntValue LEY_BATTERY_CHARGE;
    public static final ModConfigSpec.IntValue BEACON_RADIUS;
    public static final ModConfigSpec.IntValue TOOL_MANA_PER_BLOCK;
    // ---- Knowledge and familiars ----
    public static final ModConfigSpec.IntValue TORN_PAGE_KNOWLEDGE;
    public static final ModConfigSpec.IntValue BOND_PER_MINUTE;
    public static final ModConfigSpec.IntValue BOND_PER_KILL;

    static {
        BUILDER.comment("Mana and spells").push("mana");
        MANA_REGEN = BUILDER.comment("Mana regained each second once awakened: this many, or this percent of your max mana,",
            "whichever is more (the Sovereign's Crown doubles it). 0 turns natural regeneration off.")
            .defineInRange("regenPerSecond", 2, 0, 100);
        SPELL_COST = BUILDER.comment("Multiplies every spell's mana cost").defineInRange("spellCostMultiplier", 1.0, 0.0, 10.0);
        SPELL_COOLDOWN = BUILDER.comment("Multiplies every spell's cooldown").defineInRange("spellCooldownMultiplier", 1.0, 0.05, 10.0);
        BUILDER.pop();

        BUILDER.comment("Corruption").push("corruption");
        CORRUPTION_GAIN = BUILDER.comment("Multiplies corruption gained from Forbidden spells").defineInRange("gainMultiplier", 1.0, 0.0, 10.0);
        CORRUPTION_DECAY_SECONDS = BUILDER.comment("Seconds for 1 point of corruption to fade on its own")
            .defineInRange("decaySeconds", 60, 1, 3600);
        HOLY_WATER_CLEANSE = BUILDER.comment("Corruption removed by drinking Holy Water").defineInRange("holyWaterCleanse", 15, 0, 100);
        BUILDER.pop();

        BUILDER.comment("Bosses. Health applies to bosses spawned after the change.").push("bosses");
        SOVEREIGN_HEALTH = BUILDER.comment("Multiplies the Hollow Sovereign's health").defineInRange("sovereignHealth", 1.0, 0.1, 20.0);
        COLOSSUS_HEALTH = BUILDER.comment("Multiplies the Amethyst Colossus's health").defineInRange("colossusHealth", 1.0, 0.1, 20.0);
        KING_HEALTH = BUILDER.comment("Multiplies Morvane, the Revenant King's health").defineInRange("revenantKingHealth", 1.0, 0.1, 20.0);
        WIZARD_HEALTH = BUILDER.comment("Multiplies Tower Wizards' health").defineInRange("towerWizardHealth", 1.0, 0.1, 20.0);
        CASTELLAN_HEALTH = BUILDER.comment("Multiplies the Castellan's health").defineInRange("castellanHealth", 1.0, 0.1, 20.0);
        LEVIATHAN_HEALTH = BUILDER.comment("Multiplies the Leviathan's health").defineInRange("leviathanHealth", 1.0, 0.1, 20.0);
        LISTENER_HEALTH = BUILDER.comment("Multiplies the Listener's health (the final boss)").defineInRange("listenerHealth", 1.0, 0.1, 20.0);
        BOSS_DAMAGE = BUILDER.comment("Multiplies all damage dealt by bosses").defineInRange("bossDamage", 1.0, 0.1, 10.0);
        BUILDER.pop();

        BUILDER.comment("Ley lines, machines and tools").push("world");
        LEY_RITUAL_DISCOUNT = BUILDER.comment("Ritual discount at the heart of a ley line (0.5 = half price)")
            .defineInRange("leyRitualDiscount", 0.5, 0.0, 1.0);
        LEY_BATTERY_CHARGE = BUILDER.comment("Mana per second a ley line pours into a Mana Battery at full strength")
            .defineInRange("leyBatteryCharge", 2, 0, 100);
        BEACON_RADIUS = BUILDER.comment("How far the Mana Beacon reaches, in blocks").defineInRange("beaconRadius", 24, 4, 128);
        TOOL_MANA_PER_BLOCK = BUILDER.comment("Mana the Arcane tools spend on each extra block").defineInRange("toolManaPerBlock", 1, 1, 50);
        BUILDER.pop();

        BUILDER.comment("Knowledge and familiars").push("progress");
        TORN_PAGE_KNOWLEDGE = BUILDER.comment("Knowledge from a Torn Spellbook Page").defineInRange("tornPageKnowledge", 3, 0, 50);
        BOND_PER_MINUTE = BUILDER.comment("Familiar bond gained each minute together").defineInRange("bondPerMinute", 1, 0, 100);
        BOND_PER_KILL = BUILDER.comment("Familiar bond gained for each monster defeated together").defineInRange("bondPerKill", 2, 0, 100);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    // ---- Helpers used by the rest of the mod ----

    public static int manaCost(int base) {
        return base <= 0 ? base : Math.max(1, (int) Math.round(base * SPELL_COST.get()));
    }

    public static int cooldown(int base) {
        return Math.max(1, (int) Math.round(base * SPELL_COOLDOWN.get()));
    }

    public static int corruption(int base) {
        return base <= 0 ? base : (int) Math.round(base * CORRUPTION_GAIN.get());
    }
}
