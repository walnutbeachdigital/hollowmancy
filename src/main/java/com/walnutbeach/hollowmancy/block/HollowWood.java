package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

/** The full Hollow wood building set. Uses vanilla warped wood's sounds and rules (so it can't burn). */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public class HollowWood {

    private static BlockBehaviour.Properties like(Block block) {
        return BlockBehaviour.Properties.ofFullCopy(block);
    }

    // ---- Blocks ----
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_HOLLOW_LOG = Hollowmancy.BLOCKS.register("stripped_hollow_log",
            () -> new RotatedPillarBlock(like(Blocks.STRIPPED_WARPED_STEM)));
    public static final DeferredBlock<RotatedPillarBlock> HOLLOW_WOOD = Hollowmancy.BLOCKS.register("hollow_wood",
            () -> new RotatedPillarBlock(like(Blocks.WARPED_HYPHAE)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_HOLLOW_WOOD = Hollowmancy.BLOCKS.register("stripped_hollow_wood",
            () -> new RotatedPillarBlock(like(Blocks.STRIPPED_WARPED_HYPHAE)));

    public static final DeferredBlock<StairBlock> HOLLOW_STAIRS = Hollowmancy.BLOCKS.register("hollow_stairs",
            () -> new StairBlock(Hollowmancy.HOLLOW_PLANKS.get().defaultBlockState(), like(Blocks.WARPED_STAIRS)));
    public static final DeferredBlock<SlabBlock> HOLLOW_SLAB = Hollowmancy.BLOCKS.register("hollow_slab",
            () -> new SlabBlock(like(Blocks.WARPED_SLAB)));
    public static final DeferredBlock<FenceBlock> HOLLOW_FENCE = Hollowmancy.BLOCKS.register("hollow_fence",
            () -> new FenceBlock(like(Blocks.WARPED_FENCE)));
    public static final DeferredBlock<FenceGateBlock> HOLLOW_FENCE_GATE = Hollowmancy.BLOCKS.register("hollow_fence_gate",
            () -> new FenceGateBlock(WoodType.WARPED, like(Blocks.WARPED_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> HOLLOW_DOOR = Hollowmancy.BLOCKS.register("hollow_door",
            () -> new DoorBlock(BlockSetType.WARPED, like(Blocks.WARPED_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> HOLLOW_TRAPDOOR = Hollowmancy.BLOCKS.register("hollow_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.WARPED, like(Blocks.WARPED_TRAPDOOR)));
    public static final DeferredBlock<ButtonBlock> HOLLOW_BUTTON = Hollowmancy.BLOCKS.register("hollow_button",
            () -> new ButtonBlock(BlockSetType.WARPED, 30, like(Blocks.WARPED_BUTTON)));
    public static final DeferredBlock<PressurePlateBlock> HOLLOW_PRESSURE_PLATE = Hollowmancy.BLOCKS.register("hollow_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.WARPED, like(Blocks.WARPED_PRESSURE_PLATE)));

    // ---- Items ----
    public static final DeferredItem<BlockItem> STRIPPED_HOLLOW_LOG_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("stripped_hollow_log", STRIPPED_HOLLOW_LOG);
    public static final DeferredItem<BlockItem> HOLLOW_WOOD_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_wood", HOLLOW_WOOD);
    public static final DeferredItem<BlockItem> STRIPPED_HOLLOW_WOOD_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("stripped_hollow_wood", STRIPPED_HOLLOW_WOOD);
    public static final DeferredItem<BlockItem> HOLLOW_STAIRS_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_stairs", HOLLOW_STAIRS);
    public static final DeferredItem<BlockItem> HOLLOW_SLAB_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_slab", HOLLOW_SLAB);
    public static final DeferredItem<BlockItem> HOLLOW_FENCE_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_fence", HOLLOW_FENCE);
    public static final DeferredItem<BlockItem> HOLLOW_FENCE_GATE_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_fence_gate", HOLLOW_FENCE_GATE);
    public static final DeferredItem<DoubleHighBlockItem> HOLLOW_DOOR_ITEM = Hollowmancy.ITEMS.register("hollow_door",
            () -> new DoubleHighBlockItem(HOLLOW_DOOR.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> HOLLOW_TRAPDOOR_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_trapdoor", HOLLOW_TRAPDOOR);
    public static final DeferredItem<BlockItem> HOLLOW_BUTTON_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_button", HOLLOW_BUTTON);
    public static final DeferredItem<BlockItem> HOLLOW_PRESSURE_PLATE_ITEM = Hollowmancy.ITEMS.registerSimpleBlockItem("hollow_pressure_plate", HOLLOW_PRESSURE_PLATE);

    /** Called from the Hollowmancy constructor so these registrations happen in time. */
    public static void init() {
    }

    public static void fillCreativeTab(CreativeModeTab.Output output) {
        for (DeferredItem<? extends Item> item : List.of(
                STRIPPED_HOLLOW_LOG_ITEM, HOLLOW_WOOD_ITEM, STRIPPED_HOLLOW_WOOD_ITEM, HOLLOW_STAIRS_ITEM, HOLLOW_SLAB_ITEM,
                HOLLOW_FENCE_ITEM, HOLLOW_FENCE_GATE_ITEM, HOLLOW_DOOR_ITEM, HOLLOW_TRAPDOOR_ITEM,
                HOLLOW_BUTTON_ITEM, HOLLOW_PRESSURE_PLATE_ITEM)) {
            output.accept(item.get());
        }
    }

    // Right-click a Hollow Log or Hollow Wood with an axe to strip it
    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) return;
        BlockState state = event.getState();

        Block stripped = null;
        if (state.is(Hollowmancy.HOLLOW_LOG.get())) stripped = STRIPPED_HOLLOW_LOG.get();
        else if (state.is(HOLLOW_WOOD.get())) stripped = STRIPPED_HOLLOW_WOOD.get();
        if (stripped == null) return;

        event.setFinalState(stripped.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
    }
}