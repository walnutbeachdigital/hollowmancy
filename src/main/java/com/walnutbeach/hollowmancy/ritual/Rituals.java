package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.HollowmancyAdvancements;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.spell.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.walnutbeach.hollowmancy.magic.LeyLines;
import com.walnutbeach.hollowmancy.familiar.FamiliarType;
import com.walnutbeach.hollowmancy.item.hollow.HollowMaterials;
import com.walnutbeach.hollowmancy.archmage.ArchmageItems;

public class Rituals {

    private static final Map<String, Ritual> RITUALS = new LinkedHashMap<>();
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(new Vector3f(0.61f, 0.36f, 0.9f), 1.0f);

    // ---- Register every ritual here ----
    public static final Ritual INSIGHT = register(new InsightRitual());
    public static final Ritual DEEPENING = register(new DeepeningRitual());
    public static final Ritual THRESHOLD = register(new ThresholdRitual());
    public static final Ritual ASCENSION = register(new AscensionRitual());
    public static final Ritual CLEANSING = register(new CleansingRitual());
    // ---- Infusions ----
    public static final Ritual INFUSE_FOCUS_WAND = register(new InfusionRitual("infuse_focus_wand", "Infusion: Focus Wand",
            "Offer a Blaze Rod. Four lit candles 2 out on each side, four Amethyst Blocks 2 out on each diagonal.",
            MagicStage.ARCANE_ARTS, () -> Items.BLAZE_ROD, 80, Hollowmancy.FOCUS_WAND)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK));

    public static final Ritual INFUSE_WARDING_AMULET = register(new InfusionRitual("infuse_warding_amulet", "Infusion: Warding Amulet",
            "Offer a Gold Ingot. Four lit candles 2 out on each side, four Obsidian blocks 2 out on each diagonal.",
            MagicStage.ARCANE_ARTS, () -> Items.GOLD_INGOT, 80, Hollowmancy.WARDING_AMULET)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.OBSIDIAN)));

    public static final Ritual INFUSE_MANA_CRYSTAL = register(new InfusionRitual("infuse_mana_crystal", "Infusion: Mana Crystal",
            "Offer a Diamond. Four lit candles 2 out on each side, four Amethyst Blocks 2 out on each diagonal, "
                    + "and four Glimmerstone Ore 3 out on each side. Requires Mastery and 120 mana.",
            MagicStage.MASTERY, () -> Items.DIAMOND, 120, Hollowmancy.MANA_CRYSTAL)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    public static final Ritual INFUSE_ABYSSAL_FOCUS = register(new InfusionRitual("infuse_abyssal_focus", "Infusion: Abyssal Focus",
            "Offer a Focus Wand. Four lit candles 2 out on each side, four Crying Obsidian 2 out on each diagonal, "
                    + "and four Voidstone Ore 3 out on each side. Requires Ascension and 150 mana.",
            MagicStage.ASCENSION, Hollowmancy.FOCUS_WAND, 150, Hollowmancy.ABYSSAL_FOCUS)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.CRYING_OBSIDIAN))
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get())));
    public static final Ritual INFUSE_ARCANE_PICKAXE = register(new InfusionRitual("infuse_arcane_pickaxe", "Infusion: Arcane Pickaxe",
        "Offer a Diamond Pickaxe. Lit candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, "
            + "Glimmerstone Ore 3 out on each side.",
        MagicStage.ARCANE_ARTS, () -> Items.DIAMOND_PICKAXE, 100, Hollowmancy.ARCANE_PICKAXE)
        .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
        .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
        .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    public static final Ritual INFUSE_ARCANE_SHOVEL = register(new InfusionRitual("infuse_arcane_shovel", "Infusion: Arcane Shovel",
        "Offer a Diamond Shovel. Lit candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, "
            + "Glimmerstone Ore 3 out on each side.",
        MagicStage.ARCANE_ARTS, () -> Items.DIAMOND_SHOVEL, 100, Hollowmancy.ARCANE_SHOVEL)
        .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
        .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
        .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    public static final Ritual INFUSE_ARCANE_AXE = register(new InfusionRitual("infuse_arcane_axe", "Infusion: Arcane Axe",
        "Offer a Diamond Axe. Lit candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, "
            + "Glimmerstone Ore 3 out on each side.",
        MagicStage.ARCANE_ARTS, () -> Items.DIAMOND_AXE, 100, Hollowmancy.ARCANE_AXE)
        .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
        .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
        .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    public static final Ritual WAKING = register(new WakingRitual());
    public static final Ritual RESONANCE = register(new ResonanceRitual());
    public static final Ritual UNMAKING = register(new UnmakingRitual());
    public static final Ritual ECHOES = register(new EchoRitual());
    public static final Ritual INFUSE_ARCHMAGE_STAFF = register(new InfusionRitual("infuse_archmage_staff", "Infusion: Archmage's Staff",
            "Archmages only. Offer an Echo Heart. Candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, "
                    + "Gold Blocks 3 out on each side, Crying Obsidian 3 out on each diagonal.",
            MagicStage.ARCHMAGE, ArchmageItems.ECHO_HEART, 400, ArchmageItems.ARCHMAGE_STAFF)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
            .parts(RitualParts.cardinals(3), state -> state.is(Blocks.GOLD_BLOCK))
            .parts(RitualParts.diagonals(3), state -> state.is(Blocks.CRYING_OBSIDIAN)));
    public static final Ritual BIND_OWL = register(new BindingRitual(FamiliarType.OWL, "a Feather"));
    public static final Ritual BIND_WISP = register(new BindingRitual(FamiliarType.WISP, "Glowstone Dust"));
    public static final Ritual BIND_GOLEM = register(new BindingRitual(FamiliarType.GOLEM, "an Iron Block"));
    public static final Ritual BIND_MOTH = register(new HollowBindingRitual(FamiliarType.MOTH, "a Spore Blossom"));
    public static final Ritual BIND_GLIMMERHORN = register(new HollowBindingRitual(FamiliarType.GLIMMERHORN, "Glow Berries"));

    // ---- Fusions ----
    public static final Ritual FUSE_WARDED_ROBE = register(new FusionRitual("fuse_warded_robe", "Fusion: Warded Robe",
            "Offer an Arcanist's Robe while holding a Warding Amulet in your other hand. Candles 2 out on each side, "
                    + "Obsidian 2 out on each diagonal, Lapis Blocks 3 out on each side.",
            MagicStage.ARCANE_ARTS, Hollowmancy.ARCANIST_ROBE, Hollowmancy.WARDING_AMULET, 120, Hollowmancy.WARDED_ROBE)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.OBSIDIAN))
            .parts(RitualParts.cardinals(3), state -> state.is(Blocks.LAPIS_BLOCK)));

    public static final Ritual FUSE_CROWNED_HOOD = register(new FusionRitual("fuse_crowned_hood", "Fusion: Crowned Hood",
            "Offer an Arcanist's Hood while holding the Sovereign's Crown in your other hand. Candles 2 out on each side, "
                    + "Crying Obsidian 2 out on each diagonal, Voidstone Ore 3 out on each side, Amethyst Blocks 3 out on each diagonal.",
            MagicStage.ASCENSION, Hollowmancy.ARCANIST_HOOD, Hollowmancy.SOVEREIGN_CROWN, 200, Hollowmancy.CROWNED_HOOD)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.CRYING_OBSIDIAN))
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get()))
            .parts(RitualParts.diagonals(3), RitualParts.AMETHYST_BLOCK));

    public static final Ritual FUSE_WELLSPRING_WAND = register(new FusionRitual("fuse_wellspring_wand", "Fusion: Wellspring Wand",
            "Offer a Mana Crystal while holding a Focus Wand in your other hand. Candles 2 out on each side, Amethyst Blocks 2 out "
                    + "on each diagonal, Glimmerstone Ore 3 out on each side, Lapis Blocks 3 out on each diagonal.",
            MagicStage.MASTERY, Hollowmancy.MANA_CRYSTAL, Hollowmancy.FOCUS_WAND, 150, Hollowmancy.WELLSPRING_WAND)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get()))
            .parts(RitualParts.diagonals(3), state -> state.is(Blocks.LAPIS_BLOCK)));

    public static final Ritual FUSE_SKYSTRIDER_BOOTS = register(new FusionRitual("fuse_skystrider_boots", "Fusion: Skystrider Boots",
            "Offer Arcanist's Boots while holding a Phantom Membrane in your other hand. You must know Soar. Candles 2 out on each side, "
                    + "Amethyst Blocks 2 out on each diagonal, Lapis Blocks 3 out on each side, Glimmerstone Ore 3 out on each diagonal.",
            MagicStage.MASTERY, Hollowmancy.ARCANIST_BOOTS, () -> Items.PHANTOM_MEMBRANE, 200, Hollowmancy.SKYSTRIDER_BOOTS)
            .requireSpell(Spells.SOAR)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
            .parts(RitualParts.cardinals(3), state -> state.is(Blocks.LAPIS_BLOCK))
            .parts(RitualParts.diagonals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    // ---- Hollow charms: infuse a creature material ----
    public static final Ritual INFUSE_WISPBOUND_CHARM = register(new InfusionRitual("infuse_wispbound_charm", "Infusion: Wispbound Charm",
            "Offer Wisp Essence. Lit candles 2 out on each side, Amethyst Blocks 2 out on each diagonal. Requires Ascension and 120 mana.",
            MagicStage.ASCENSION, HollowMaterials.WISP_ESSENCE, 120, HollowMaterials.WISPBOUND_CHARM)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK));

    public static final Ritual INFUSE_MOTHWING_CHARM = register(new InfusionRitual("infuse_mothwing_charm", "Infusion: Mothwing Charm",
            "Offer Moth Wing Dust. Lit candles 2 out on each side, Glowstone 2 out on each diagonal. Requires Ascension and 120 mana.",
            MagicStage.ASCENSION, HollowMaterials.MOTH_DUST, 120, HollowMaterials.MOTHWING_CHARM)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.GLOWSTONE)));

    public static final Ritual INFUSE_HUNTSMAN_CHARM = register(new InfusionRitual("infuse_huntsman_charm", "Infusion: Huntsman's Charm",
            "Offer a Glimmerhorn Antler Shard. Lit candles 2 out on each side, Amethyst Blocks 2 out on each diagonal, "
                    + "Glimmerstone Ore 3 out on each side. Requires Ascension and 140 mana.",
            MagicStage.ASCENSION, HollowMaterials.ANTLER_SHARD, 140, HollowMaterials.HUNTSMAN_CHARM)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), RitualParts.AMETHYST_BLOCK)
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.GLIMMERSTONE_ORE.get())));

    public static final Ritual INFUSE_SHADEWEAVE_CHARM = register(new InfusionRitual("infuse_shadeweave_charm", "Infusion: Shadeweave Charm",
            "Offer Shade Cloth. Lit candles 2 out on each side, Crying Obsidian 2 out on each diagonal, "
                    + "Voidstone Ore 3 out on each side. Requires Ascension and 160 mana.",
            MagicStage.ASCENSION, HollowMaterials.SHADE_CLOTH, 160, HollowMaterials.SHADEWEAVE_CHARM)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.CRYING_OBSIDIAN))
            .parts(RitualParts.cardinals(3), state -> state.is(Hollowmancy.VOIDSTONE_ORE.get())));

    public static final Ritual INFUSE_TIDEBOUND_CHARM = register(new InfusionRitual("infuse_tidebound_charm", "Infusion: Tidebound Charm",
            "Offer a Lurker Scale. Lit candles 2 out on each side, Prismarine 2 out on each diagonal. Requires Ascension and 120 mana.",
            MagicStage.ASCENSION, HollowMaterials.LURKER_SCALE, 120, HollowMaterials.TIDEBOUND_CHARM)
            .parts(RitualParts.cardinals(2), RitualParts.LIT_CANDLE)
            .parts(RitualParts.diagonals(2), state -> state.is(Blocks.PRISMARINE)));

    private static Ritual register(Ritual ritual) {
        RITUALS.put(ritual.getId(), ritual);
        return ritual;
    }

    public static Collection<Ritual> all() {
        return RITUALS.values();
    }

    public static Ritual findByOffering(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Ritual ritual : RITUALS.values()) {
            if (stack.is(ritual.getOffering())) return ritual;
        }
        return null;
    }

    /** Empty-hand right-click: list the rituals this player can perform. */
    public static void describe(ServerPlayer player) {
        MagicData data = MagicHelper.get(player);
        if (data.stage() < MagicStage.APPRENTICE) {
            player.displayClientMessage(Component.translatable("hollowmancy.rituals.the_altar_is_cold_and_silent")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return;
        }

        player.sendSystemMessage(Component.translatable("hollowmancy.rituals.rituals_known_to_you").withStyle(ChatFormatting.GOLD));
        for (Ritual ritual : RITUALS.values()) {
            if (data.stage() < ritual.getRequiredStage()) continue;
            player.sendSystemMessage(Component.literal(ritual.getName()).withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("hollowmancy.rituals.mana", ritual.getManaCost()).withStyle(ChatFormatting.DARK_GRAY)));
            player.sendSystemMessage(Component.literal("  " + ritual.getHowTo()).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Right-click with an offering: check everything, then perform. */
    public static void activate(ServerPlayer player, ServerLevel level, BlockPos altar, ItemStack offering) {
        Ritual ritual = findByOffering(offering);
        if (ritual == null) return;

        MagicData data = MagicHelper.get(player);
        if (data.stage() < ritual.getRequiredStage()) {
            player.displayClientMessage(Component.translatable("hollowmancy.rituals.you_do_not_understand_what_the")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return;
        }

        List<BlockPos> missing = ritual.findMissing(level, altar);
        if (!missing.isEmpty()) {
            for (BlockPos pos : missing) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        8, 0.2, 0.2, 0.2, 0.01);
            }
            player.displayClientMessage(Component.translatable("hollowmancy.rituals.the_pattern_is_incomplete_smoke_marks", ritual.getName())
                    .withStyle(ChatFormatting.GRAY), true);
            level.playSound(null, altar, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }

        if (data.mana() < LeyLines.ritualCost(level, altar, ritual.getManaCost())) {
            player.displayClientMessage(Component.translatable("hollowmancy.rituals.not_enough_mana_needs", LeyLines.ritualCost(level, altar, ritual.getManaCost()))
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!ritual.canPerform(player)) return;

        // Pay the costs
        ItemStack offeringCopy = offering.copy();
        if (!player.getAbilities().instabuild) offering.shrink(1);
        MagicHelper.set(player, data.withMana(data.mana() - LeyLines.ritualCost(level, altar, ritual.getManaCost())));
        LeyLines.announceDiscount(player, level, altar);

        // Beams from each pattern piece into the altar; snuff out candles
        Vec3 center = Vec3.atCenterOf(altar).add(0, 0.7, 0);
        for (BlockPos offset : ritual.getPattern().keySet()) {
            BlockPos partPos = altar.offset(offset);
            drawBeam(level, Vec3.atCenterOf(partPos), center);

            BlockState state = level.getBlockState(partPos);
            if (state.hasProperty(CandleBlock.LIT)) {
                level.setBlock(partPos, state.setValue(CandleBlock.LIT, false), Block.UPDATE_ALL);
            }
        }

        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 30, 0.1, 0.3, 0.1, 0.08);
        level.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 20, 0.3, 0.3, 0.3, 0.1);
        level.playSound(null, altar, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        level.playSound(null, altar, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.8f, 1.0f);

        ritual.performWithOffering(player, level, altar, offeringCopy);
        HollowmancyAdvancements.award(player, "first_ritual", "performed");
    }

    private static void drawBeam(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 path = to.subtract(from);
        int points = Math.max(1, (int) (path.length() * 4));
        for (int i = 0; i <= points; i++) {
            Vec3 point = from.add(path.scale((double) i / points));
            level.sendParticles(PURPLE_DUST, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }
}