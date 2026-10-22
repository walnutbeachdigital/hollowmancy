package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.item.ArcanistGear;
import com.walnutbeach.hollowmancy.item.Artifacts;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.network.SpellCooldownPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.walnutbeach.hollowmancy.HollowmancyConfig;
import net.minecraft.locale.Language;
import com.walnutbeach.hollowmancy.item.WandMastery;
import com.walnutbeach.hollowmancy.archmage.LegendarySpells;

public class Spells {

    private static final Map<String, Spell> SPELLS = new LinkedHashMap<>();
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    // ---- Upgrades ----
    public static final String TIER_PREFIX = "upgrade:";
    public static final int TIER_II_COST = 4;
    public static final int TIER_III_COST = 8;
    public static final int TIER_III_STAGE = MagicStage.MASTERY;
    public static final int TIER_IV_COST = 12;
    public static final int TIER_IV_STAGE = MagicStage.ARCHMAGE;
    public static final String TIER_IV_TEXT = "45% cheaper and 55% faster to recharge than its first form.";

    /** TIER_IV_TEXT in the player's language. */
    public static String tierIvText() {
        return Language.getInstance().getOrDefault("hollowmancy.spells.tier_iv_text", TIER_IV_TEXT);
    }

    // ---- General ----
    public static final Spell SPARK = register(new SparkSpell());
    public static final Spell MEND = register(new MendSpell());
    public static final Spell BLINK = register(new BlinkSpell());

    // ---- Elemental ----
    public static final Spell FIREBOLT = register(new FireboltSpell());
    public static final Spell FROST_NOVA = register(new FrostNovaSpell());
    public static final Spell STORMCALL = register(new StormcallSpell());
    public static final Spell SOAR = register(new SoarSpell());

    // ---- Warding ----
    public static final Spell AEGIS = register(new AegisSpell());
    public static final Spell REPEL = register(new RepelSpell());
    public static final Spell SANCTUARY = register(new SanctuarySpell());
    public static final Spell REFLECT = register(new ReflectSpell());
    public static final java.util.List<Spell> CRAFTED = CraftedSpell.registerAll(Spells::register);

    // ---- Divination ----
    public static final Spell REVEAL = register(new RevealSpell());
    public static final Spell DOWSING = register(new DowsingSpell());
    public static final Spell RECALL = register(new RecallSpell());
    public static final Spell LEY_SIGHT = register(new LeySightSpell());
    public static final Spell HUNTERS_MARK = register(new HuntersMarkSpell());

    // ---- Nature ----
    public static final Spell VERDANT_GROWTH = register(new VerdantGrowthSpell());
    public static final Spell WELLSPRING = register(new WellspringSpell());
    public static final Spell CALL_OF_THE_WILD = register(new CallOfTheWildSpell());
    public static final Spell BRAMBLE_SNARE = register(new BrambleSnareSpell());

    // ---- Forbidden ----
    public static final Spell VOID_LANCE = register(new VoidLanceSpell());
    public static final Spell SIPHON = register(new SiphonSpell());
    public static final Spell VEIL_STEP = register(new VeilStepSpell());

    private static Spell register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
        return spell;
    }

    public static Spell get(String id) {
        return SPELLS.get(id);
    }

    public static Collection<Spell> all() {
        return SPELLS.values();
    }

    public static List<Spell> inSchool(MagicSchool school) {
        return all().stream().filter(spell -> spell.getSchool() == school && !(spell instanceof CraftedSpell)).toList();
    }

    public static boolean isKnown(MagicData data, Spell spell) {
        if (data.stage() < spell.getRequiredStage()) return false;
        if (!spell.getSchool().isUnlocked(data)) return false;
        return spell.getKnowledgeCost() == 0 || data.learned().contains(spell.getId());
    }

    public static List<Spell> known(MagicData data) {
        return all().stream().filter(spell -> isKnown(data, spell)).toList();
    }

    // ---------------- Tiers ----------------

    public static int getTier(MagicData data, Spell spell) {
        if (data.learned().contains("tier4:" + spell.getId())) return 4;
        if (data.learned().contains("tier3:" + spell.getId())) return 3;
        if (data.learned().contains("tier2:" + spell.getId())) return 2;
        return 1;
    }

    public static String tierNumeral(int tier) {
        return tier == 4 ? "IV" : tier == 3 ? "III" : tier == 2 ? "II" : "";
    }

    public static String displayName(Spell spell, MagicData data) {
        int tier = getTier(data, spell);
        return tier > 1 ? spell.getName() + " " + tierNumeral(tier) : spell.getName();
    }

    public static int effectiveManaCost(int tier, int baseCost) {
        float multiplier = tier >= 4 ? 0.55f : tier == 3 ? 0.70f : tier == 2 ? 0.85f : 1.0f;
        return Math.max(1, Math.round(baseCost * multiplier));
    }

    public static int effectiveCooldown(int tier, int baseTicks) {
        float multiplier = tier >= 4 ? 0.45f : tier == 3 ? 0.60f : tier == 2 ? 0.80f : 1.0f;
        return Math.max(1, Math.round(baseTicks * multiplier));
    }

    public static int effectiveCharge(int tier, int baseTicks) {
        if (baseTicks <= 0) return 0;
        float multiplier = tier >= 4 ? 0.55f : tier == 3 ? 0.70f : tier == 2 ? 0.85f : 1.0f;
        return Math.max(1, Math.round(baseTicks * multiplier));
    }

    public static int upgradeCost(int nextTier) {
        return nextTier == 4 ? TIER_IV_COST : nextTier == 3 ? TIER_III_COST : TIER_II_COST;
    }

    public static boolean canUpgrade(MagicData data, Spell spell) {
        if (!isKnown(data, spell)) return false;
        int next = getTier(data, spell) + 1;
        if (next > 4) return false;
        if (next == 4 && data.stage() < TIER_IV_STAGE) return false;
        if (next == 3 && data.stage() < TIER_III_STAGE) return false;
        return data.knowledge() >= upgradeCost(next);
    }

    // ---------------- Casting ----------------

    /** Server side: the player pressed cast. Check everything, then fire instantly or begin channeling. */
    public static void tryCast(ServerPlayer player, String spellId) {
        // Pressing cast while channeling lets the spell go
        if (Channeling.isChanneling(player)) {
            Channeling.cancel(player, "You let the spell go.");
            return;
        }

        Spell spell = get(spellId);
        if (spell == null) return;

        MagicData data = MagicHelper.get(player);
        if (!isKnown(data, spell)) return;

        int tier = getTier(data, spell);
        int cooldown = Artifacts.prismCooldown(player, ArcanistGear.cooldown(player, HollowmancyConfig.cooldown(effectiveCooldown(tier, spell.getCooldownTicks()))));

        long now = player.serverLevel().getGameTime();
        Long lastCast = COOLDOWNS.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>()).get(spellId);
        if (lastCast != null && now - lastCast < cooldown) {
            int remaining = (int) (cooldown - (now - lastCast));
            PacketDistributor.sendToPlayer(player, new SpellCooldownPayload(spellId, remaining, cooldown));
            return;
        }

        if (Artifacts.availableMana(player) < HollowmancyConfig.manaCost(manaCostFor(player, spell, tier))) {
            notEnoughMana(player);
            return;
        }

        int charge = ArcanistGear.charge(player, effectiveCharge(tier, spell.getChargeTicks()));
        if (charge > 0) {
            Channeling.start(player, spell, charge);
        } else {
            release(player, spell);
        }
    }

    /** Actually cast the spell and pay for it. Called instantly, or when a channel completes. */
    public static void release(ServerPlayer player, Spell spell) {
        MagicData data = MagicHelper.get(player);
        int tier = getTier(data, spell);
        int manaCost = HollowmancyConfig.manaCost(manaCostFor(player, spell, tier));

        // Mana is checked again: it may have changed during a channel
        if (Artifacts.availableMana(player) < manaCost) {
            notEnoughMana(player);
            return;
        }

        if (!spell.cast(player)) return; // fizzled: no cost, no cooldown

        WandMastery.onCast(player);

        LegendarySpells.afterCast(player, spell);

        // Every school has its own voice, layered under the spell's own sounds
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.castFor(spell.getSchool()),
                SoundSource.PLAYERS, 0.55f, 0.95f + player.getRandom().nextFloat() * 0.1f);

        int cooldown = Artifacts.prismCooldown(player, ArcanistGear.cooldown(player, HollowmancyConfig.cooldown(effectiveCooldown(tier, spell.getCooldownTicks()))));
        COOLDOWNS.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>())
                .put(spell.getId(), player.serverLevel().getGameTime());
        PacketDistributor.sendToPlayer(player, new SpellCooldownPayload(spell.getId(), cooldown, cooldown));

        int fromPlayer = Artifacts.drawFromWand(player, manaCost);
        MagicData latest = MagicHelper.get(player);
        MagicHelper.set(player, latest
                .withMana(latest.mana() - fromPlayer)
                .withCorruption(latest.corruption() + HollowmancyConfig.corruption(Artifacts.effectiveCorruption(player, spell.getCorruption()))));
    }

    private static int manaCostFor(ServerPlayer player, Spell spell, int tier) {
        return Artifacts.effectiveManaCost(player, effectiveManaCost(tier, spell.getManaCost()));
    }

    private static void notEnoughMana(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("hollowmancy.spells.not_enough_mana").withStyle(ChatFormatting.RED), true);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SPELL_FIZZLE.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    // ---------------- Learning ----------------

    /** Server side: learn a spell, unlock a school ("school:..."), or upgrade a spell ("upgrade:..."). */
    public static void tryLearn(ServerPlayer player, String id) {
        if (TomeSpells.refuse(player, id)) return;
        if (id.startsWith(MagicSchool.PREFIX)) {
            MagicSchool.tryUnlock(player, id);
            return;
        }
        if (id.startsWith(TIER_PREFIX)) {
            tryUpgrade(player, id.substring(TIER_PREFIX.length()));
            return;
        }

        Spell spell = get(id);
        if (spell == null) return;

        MagicData data = MagicHelper.get(player);
        if (isKnown(data, spell)) return;
        if (data.stage() < spell.getRequiredStage()) return;
        if (!spell.getSchool().isUnlocked(data)) return;
        if (data.knowledge() < spell.getKnowledgeCost()) return;

        MagicHelper.set(player, data
                .withKnowledge(data.knowledge() - spell.getKnowledgeCost())
                .withLearned(spell.getId()));

        player.sendSystemMessage(Component.translatable("hollowmancy.spells.you_have_learned", spell.nameComponent())
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SPELL_LEARN.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
    }

    private static void tryUpgrade(ServerPlayer player, String spellId) {
        Spell spell = get(spellId);
        if (spell == null) return;

        MagicData data = MagicHelper.get(player);
        if (!canUpgrade(data, spell)) return;

        int next = getTier(data, spell) + 1;
        MagicHelper.set(player, data
                .withKnowledge(data.knowledge() - upgradeCost(next))
                .withLearned("tier" + next + ":" + spell.getId()));

        player.sendSystemMessage(Component.translatable("hollowmancy.spells.your_mastery_deepens", spell.nameComponent(), tierNumeral(next))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SPELL_TIER_UP.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
    }
}