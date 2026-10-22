package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

/**
 * The Arcane Pickaxe, Shovel and Axe. Diamond-strength tools that spend mana:
 * the pickaxe and shovel dig 3x3, the axe fells a whole tree. Sneak to dig a single block for free.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class ArcaneTools {

    public static final int MANA_PER_BLOCK = 1;   // mana for each extra block
    public static final int MAX_TREE = 64;        // most logs one swing can fell

    /** Diamond mining level, more durable, repaired with Mana Crystals. */
    public static final Tier TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
        2000, 9.0f, 3.5f, 18, () -> Ingredient.of(Hollowmancy.MANA_CRYSTAL.get()));

    private static boolean breaking = false;      // stops our own extra breaks from triggering more

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (breaking || event.isCanceled()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.isShiftKeyDown()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ItemStack tool = player.getMainHandItem();
        BlockPos origin = event.getPos();
        BlockState originState = event.getState();
        if (!tool.isCorrectToolForDrops(originState)) return;

        List<BlockPos> extra;
        if (tool.is(Hollowmancy.ARCANE_PICKAXE.get()) || tool.is(Hollowmancy.ARCANE_SHOVEL.get())) {
            extra = area(player, level, origin, originState, tool);
        } else if (tool.is(Hollowmancy.ARCANE_AXE.get()) && originState.is(BlockTags.LOGS)) {
            extra = tree(level, origin);
        } else {
            return;
        }
        if (extra.isEmpty()) return;

        MagicData data = MagicHelper.get(player);
        int affordable = player.getAbilities().instabuild ? extra.size() : data.mana() / HollowmancyConfig.TOOL_MANA_PER_BLOCK.get();
        if (affordable <= 0) {
            player.displayClientMessage(Component.translatable("hollowmancy.arcane_tools.not_enough_mana_the_tool_digs")
                .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        int broken = 0;
        breaking = true;
        try {
            for (BlockPos pos : extra) {
                if (broken >= affordable || tool.isEmpty()) break;
                if (player.gameMode.destroyBlock(pos)) broken++;
            }
        } finally {
            breaking = false;
        }
        if (broken > 0 && !player.getAbilities().instabuild) {
            MagicData latest = MagicHelper.get(player);
            MagicHelper.set(player, latest.withMana(latest.mana() - broken * HollowmancyConfig.TOOL_MANA_PER_BLOCK.get()));
        }
    }

    /** The 3x3 square around the block, facing whichever side the player is mining. */
    private static List<BlockPos> area(ServerPlayer player, ServerLevel level, BlockPos origin, BlockState originState,
                                       ItemStack tool) {
        Direction face = minedFace(player, origin);
        float originHardness = originState.getDestroySpeed(level, origin);
        List<BlockPos> result = new ArrayList<>();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos pos = switch (face.getAxis()) {
                    case Y -> origin.offset(a, 0, b);
                    case X -> origin.offset(0, a, b);
                    case Z -> origin.offset(a, b, 0);
                };
                BlockState state = level.getBlockState(pos);
                float hardness = state.getDestroySpeed(level, pos);
                if (state.isAir() || hardness < 0 || level.getBlockEntity(pos) != null) continue;
                if (hardness > originHardness + 1.5f) continue;   // no free obsidian while mining stone
                if (!tool.isCorrectToolForDrops(state)) continue;
                result.add(pos);
            }
        }
        return result;
    }

    private static Direction minedFace(ServerPlayer player, BlockPos origin) {
        Vec3 eye = player.getEyePosition();
        Vec3 reach = eye.add(player.getLookAngle().scale(player.blockInteractionRange() + 1.0));
        HitResult hit = player.level().clip(new ClipContext(eye, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(origin)) return blockHit.getDirection();
        return Direction.orderedByNearest(player)[0].getOpposite();
    }

    /** Every log touching this one (diagonals included), nearest first, up to MAX_TREE. */
    private static List<BlockPos> tree(ServerLevel level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty() && result.size() < MAX_TREE) {
            BlockPos current = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (next.getY() < origin.getY()) continue;   // never dig below where you swung
                BlockPos pos = next.immutable();
                if (!seen.add(pos) || !level.getBlockState(pos).is(BlockTags.LOGS)) continue;
                result.add(pos);
                queue.add(pos);
                if (result.size() >= MAX_TREE) break;
            }
        }
        return touchesNaturalLeaves(level, origin, result) ? result : List.of();
    }

    /** Only real trees: some log must touch leaves that grew there (not leaves a player placed). */
    private static boolean touchesNaturalLeaves(ServerLevel level, BlockPos origin, List<BlockPos> logs) {
        List<BlockPos> all = new ArrayList<>(logs);
        all.add(origin);
        for (BlockPos log : all) {
            for (BlockPos near : BlockPos.betweenClosed(log.offset(-1, -1, -1), log.offset(1, 1, 1))) {
                BlockState state = level.getBlockState(near);
                if (state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT)
                        && !state.getValue(LeavesBlock.PERSISTENT)) return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String line;
        if (stack.is(Hollowmancy.ARCANE_PICKAXE.get()) || stack.is(Hollowmancy.ARCANE_SHOVEL.get())) {
            line = "Digs 3x3 (" + MANA_PER_BLOCK + " mana per extra block)";
        } else if (stack.is(Hollowmancy.ARCANE_AXE.get())) {
            line = "Fells the whole tree (" + MANA_PER_BLOCK + " mana per extra log)";
        } else {
            return;
        }
        event.getToolTip().add(1, Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE));
        event.getToolTip().add(2, Component.translatable("hollowmancy.arcane_tools.sneak_to_dig_one_block").withStyle(ChatFormatting.GRAY));
    }
}
