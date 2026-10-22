package com.walnutbeach.hollowmancy.compat.jei;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.ritual.FusionRitual;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import com.walnutbeach.hollowmancy.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** One ritual, worked out into what JEI shows: a pattern map, the parts list, the result, and a summary. */
public record RitualDisplay(Ritual ritual, Map<BlockPos, Item> grid, List<ItemStack> parts,
                            ItemStack result, String effect, int radius) {

    /** A block a pattern might ask for, and the item that represents it. Add new pattern blocks here. */
    private record Candidate(BlockState state, Item item) {}

    private static List<Candidate> candidates() {
        return List.of(
                new Candidate(Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true), Items.CANDLE),
                new Candidate(Blocks.AMETHYST_BLOCK.defaultBlockState(), Items.AMETHYST_BLOCK),
                new Candidate(Blocks.LAPIS_BLOCK.defaultBlockState(), Items.LAPIS_BLOCK),
                new Candidate(Blocks.OBSIDIAN.defaultBlockState(), Items.OBSIDIAN),
                new Candidate(Blocks.CRYING_OBSIDIAN.defaultBlockState(), Items.CRYING_OBSIDIAN),
                new Candidate(Hollowmancy.GLIMMERSTONE_ORE.get().defaultBlockState(), Hollowmancy.GLIMMERSTONE_ORE_ITEM.get()),
                new Candidate(Hollowmancy.VOIDSTONE_ORE.get().defaultBlockState(), Hollowmancy.VOIDSTONE_ORE_ITEM.get()),
            new Candidate(Blocks.GOLD_BLOCK.defaultBlockState(), Items.GOLD_BLOCK)
        );
    }

    /** What non-infusion rituals do. Add a line here when you add a ritual. */
    private static final Map<String, String> EFFECTS = Map.of(
            "insight", "+3 Knowledge.",
            "deepening", "+25 max mana (up to 300).",
            "threshold", "Costs 10 Knowledge. Advance to The Arcane Arts.",
            "ascension", "Needs 200 max mana. Costs 20 Knowledge. Ascend, with +50 max mana.",
            "cleansing", "Removes 50 corruption.",
            "waking", "Only in the Hollow. Wakes the Hollow Sovereign."
    );

    public static RitualDisplay from(Ritual ritual) {
        List<Candidate> candidates = candidates();
        Map<BlockPos, Item> grid = new LinkedHashMap<>();
        Map<Item, Integer> counts = new LinkedHashMap<>();
        int radius = 1;

        for (Map.Entry<BlockPos, Predicate<BlockState>> part : ritual.getPattern().entrySet()) {
            Item item = Items.BARRIER; // shown if the block isn't one of the candidates
            for (Candidate candidate : candidates) {
                if (part.getValue().test(candidate.state())) {
                    item = candidate.item();
                    break;
                }
            }
            grid.put(part.getKey(), item);
            counts.merge(item, 1, Integer::sum);
            radius = Math.max(radius, Math.max(Math.abs(part.getKey().getX()), Math.abs(part.getKey().getZ())));
        }

        List<ItemStack> parts = new ArrayList<>();
        counts.forEach((item, count) -> parts.add(new ItemStack(item, count)));

        ItemStack result = ItemStack.EMPTY;
        String effect = JeiExtras.effectOf(ritual.getId(), EFFECTS);
        if (ritual instanceof InfusionRitual infusion) {
            result = new ItemStack(infusion.getResult());
            effect = "Reforges the offering.";
        } else if (ritual instanceof FusionRitual fusion) {
            result = new ItemStack(fusion.getResult());
            ItemStack catalyst = new ItemStack(fusion.getCatalyst());
            parts.add(0, catalyst); // shown first in the parts row
            effect = "Hold the " + catalyst.getHoverName().getString() + " in your other hand.";
        }

        return new RitualDisplay(ritual, grid, parts, result, effect, radius);
    }
}