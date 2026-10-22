package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Decorative blocks made from Hollow materials. */
public final class HollowBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Hollowmancy.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hollowmancy.MODID);

    /**
     * A small lamp: a crystal cluster on a silver base. Like a lantern it stands on the ground, or hangs upside down
     * from the block above when you place it against a ceiling. It pops off if what holds it is broken.
     */
    public static class CrystalLampBlock extends Block {
        public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
        private static final VoxelShape STANDING_SHAPE = Block.box(4, 0, 4, 12, 14, 12);
        private static final VoxelShape HANGING_SHAPE = Block.box(4, 2, 4, 12, 16, 12);

        public CrystalLampBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(HANGING, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(HANGING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            for (Direction direction : context.getNearestLookingDirections()) {
                if (direction.getAxis() != Direction.Axis.Y) continue;
                BlockState state = defaultBlockState().setValue(HANGING, direction == Direction.UP);
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
            }
            return null;
        }

        /** The side it's held from: the block above when hanging, the block below when standing. */
        private static Direction support(BlockState state) {
            return state.getValue(HANGING) ? Direction.UP : Direction.DOWN;
        }

        @Override
        protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            Direction side = support(state);
            return Block.canSupportCenter(level, pos.relative(side), side.getOpposite());
        }

        @Override
        protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                         BlockPos pos, BlockPos neighborPos) {
            if (direction == support(state) && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return state.getValue(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
        }
    }

    public static final DeferredBlock<CrystalLampBlock> CRYSTAL_LAMP = BLOCKS.register("crystal_lamp",
            () -> new CrystalLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.6F)
                    .sound(SoundType.AMETHYST).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<LanternBlock> WISP_LANTERN = BLOCKS.register("wisp_lantern",
            () -> new LanternBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F)
                    .sound(SoundType.LANTERN).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> SHADEWEAVE = BLOCKS.register("shadeweave",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.8F).sound(SoundType.WOOL)));
    public static final DeferredBlock<CarpetBlock> SHADEWEAVE_CARPET = BLOCKS.register("shadeweave_carpet",
            () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.1F).sound(SoundType.WOOL)));
    public static final DeferredBlock<Block> LURKER_SCALE_TILES = BLOCKS.register("lurker_scale_tiles",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE_TILES)));
    public static final DeferredBlock<SlabBlock> LURKER_SCALE_TILE_SLAB = BLOCKS.register("lurker_scale_tile_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(LURKER_SCALE_TILES.get())));
    public static final DeferredBlock<StairBlock> LURKER_SCALE_TILE_STAIRS = BLOCKS.register("lurker_scale_tile_stairs",
            () -> new StairBlock(LURKER_SCALE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LURKER_SCALE_TILES.get())));

    public static final DeferredItem<?> CRYSTAL_LAMP_ITEM = ITEMS.registerSimpleBlockItem("crystal_lamp", CRYSTAL_LAMP);
    public static final DeferredItem<?> WISP_LANTERN_ITEM = ITEMS.registerSimpleBlockItem("wisp_lantern", WISP_LANTERN);
    public static final DeferredItem<?> SHADEWEAVE_ITEM = ITEMS.registerSimpleBlockItem("shadeweave", SHADEWEAVE);
    public static final DeferredItem<?> SHADEWEAVE_CARPET_ITEM = ITEMS.registerSimpleBlockItem("shadeweave_carpet", SHADEWEAVE_CARPET);
    public static final DeferredItem<?> LURKER_SCALE_TILES_ITEM = ITEMS.registerSimpleBlockItem("lurker_scale_tiles", LURKER_SCALE_TILES);
    public static final DeferredItem<?> LURKER_SCALE_TILE_SLAB_ITEM = ITEMS.registerSimpleBlockItem("lurker_scale_tile_slab", LURKER_SCALE_TILE_SLAB);
    public static final DeferredItem<?> LURKER_SCALE_TILE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem("lurker_scale_tile_stairs", LURKER_SCALE_TILE_STAIRS);

    private static final List<DeferredItem<?>> ALL = List.of(CRYSTAL_LAMP_ITEM, WISP_LANTERN_ITEM, SHADEWEAVE_ITEM, SHADEWEAVE_CARPET_ITEM,
            LURKER_SCALE_TILES_ITEM, LURKER_SCALE_TILE_SLAB_ITEM, LURKER_SCALE_TILE_STAIRS_ITEM);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    public static void addToTab(CreativeModeTab.Output output) {
        ALL.forEach(item -> output.accept(item.get()));
    }

    private HollowBlocks() {}
}
