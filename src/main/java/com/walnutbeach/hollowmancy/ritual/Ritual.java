package com.walnutbeach.hollowmancy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Ritual {

    private final String id;
    private final String name;
    private final String howTo;
    private final int requiredStage;
    private final Supplier<? extends Item> offering;
    private final int manaCost;

    // Offset from the altar -> what must be there
    private final Map<BlockPos, Predicate<BlockState>> pattern = new LinkedHashMap<>();

    protected Ritual(String id, String name, String howTo, int requiredStage,
                     Supplier<? extends Item> offering, int manaCost) {
        this.id = id;
        this.name = name;
        this.howTo = howTo;
        this.requiredStage = requiredStage;
        this.offering = offering;
        this.manaCost = manaCost;
    }

    protected void require(BlockPos offset, Predicate<BlockState> test) {
        pattern.put(offset, test);
    }

    protected void requireAll(List<BlockPos> offsets, Predicate<BlockState> test) {
        for (BlockPos offset : offsets) require(offset, test);
    }

    public String getId() { return id; }
    /** In the player's language (keys hollowmancy.ritual.<id> and hollowmancy.ritual.<id>.howto); English if there's no translation. */
    public String getName() { return net.minecraft.locale.Language.getInstance().getOrDefault("hollowmancy.ritual." + id, name); }
    public String getHowTo() { return net.minecraft.locale.Language.getInstance().getOrDefault("hollowmancy.ritual." + id + ".howto", howTo); }
    public int getRequiredStage() { return requiredStage; }
    public Item getOffering() { return offering.get(); }
    public int getManaCost() { return manaCost; }
    public Map<BlockPos, Predicate<BlockState>> getPattern() { return pattern; }

    /** World positions of every pattern piece that is missing or wrong. Empty list = pattern complete. */
    public List<BlockPos> findMissing(Level level, BlockPos altar) {
        List<BlockPos> missing = new ArrayList<>();
        for (Map.Entry<BlockPos, Predicate<BlockState>> part : pattern.entrySet()) {
            BlockPos pos = altar.offset(part.getKey());
            if (!part.getValue().test(level.getBlockState(pos))) missing.add(pos);
        }
        return missing;
    }

    /** Extra check before anything is consumed. Return false (and tell the player why) to cancel. */
    public boolean canPerform(ServerPlayer player) {
        return true;
    }

    /** The ritual's effect. Mana and the offering have already been taken. */
    public abstract void perform(ServerPlayer player, ServerLevel level, BlockPos altar);

    /** Like perform, but also given a copy of the offering (taken before it was used up). Fusions use this. */
    public void performWithOffering(ServerPlayer player, ServerLevel level, BlockPos altar, ItemStack offering) {
        perform(player, level, altar);
    }
}