package com.walnutbeach.hollowmancy;

import com.walnutbeach.hollowmancy.block.*;
import com.walnutbeach.hollowmancy.entity.ModEntities;
import com.walnutbeach.hollowmancy.item.*;
import com.walnutbeach.hollowmancy.menu.ModMenus;
import com.walnutbeach.hollowmancy.villager.ModVillagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.walnutbeach.hollowmancy.magic.ModAttachments;
import vazkii.patchouli.api.PatchouliAPI;
import com.walnutbeach.hollowmancy.entity.hollow.HollowCreatures;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import com.walnutbeach.hollowmancy.item.hollow.HollowMaterials;
import com.walnutbeach.hollowmancy.worldgen.ConfiguredSpreadPlacement;
import com.walnutbeach.hollowmancy.archmage.ArchmageItems;
import com.walnutbeach.hollowmancy.familiar.HollowFamiliars;
import com.walnutbeach.hollowmancy.entity.castle.CastleGuards;
import com.walnutbeach.hollowmancy.sound.ModSounds;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(com.walnutbeach.hollowmancy.Hollowmancy.MODID)
public class Hollowmancy {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hollowmancy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredBlock<DropExperienceBlock> GLIMMERSTONE_ORE = BLOCKS.register("glimmerstone_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                            .strength(3.0f, 3.0f)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 3)));

    public static final DeferredItem<FocusWandItem> FOCUS_WAND = ITEMS.register("focus_wand",
            () -> new FocusWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<WardingAmuletItem> WARDING_AMULET = ITEMS.register("warding_amulet",
            () -> new WardingAmuletItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<ManaCrystalItem> MANA_CRYSTAL = ITEMS.register("mana_crystal",
            () -> new ManaCrystalItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredBlock<DropExperienceBlock> VOIDSTONE_ORE = BLOCKS.register("voidstone_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 8),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE)
                            .strength(4.5f, 9.0f)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 2)));

    public static final DeferredItem<BlockItem> VOIDSTONE_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("voidstone_ore", VOIDSTONE_ORE);

    public static final DeferredItem<Item> VOID_SHARD =
            ITEMS.registerSimpleItem("void_shard", new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<AbyssalFocusItem> ABYSSAL_FOCUS = ITEMS.register("abyssal_focus",
            () -> new AbyssalFocusItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<LostPageItem> LOST_PAGE = ITEMS.register("lost_page",
            () -> new LostPageItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<SpellbookItem> SPELLBOOK = ITEMS.register("spellbook",
            () -> new SpellbookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> VOID_HEART =
            ITEMS.registerSimpleItem("void_heart", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<SovereignCrownItem> SOVEREIGN_CROWN = ITEMS.register("sovereign_crown",
            () -> new SovereignCrownItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<DeferredSpawnEggItem> HOLLOW_SOVEREIGN_SPAWN_EGG = ITEMS.register("hollow_sovereign_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HOLLOW_SOVEREIGN, 0x1A1028, 0xB0263F, new Item.Properties()));

    public static final DeferredItem<ArcanistArmorItem> ARCANIST_HOOD = ITEMS.register("arcanist_hood",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15)),
                    "Spells cost 10% less mana"));

    public static final DeferredItem<ArcanistArmorItem> ARCANIST_ROBE = ITEMS.register("arcanist_robe",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15)),
                    "Forbidden spells leave 25% less corruption"));

    public static final DeferredItem<ArcanistArmorItem> ARCANIST_LEGGINGS = ITEMS.register("arcanist_leggings",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15)),
                    "Mana returns 50% faster"));

    public static final DeferredItem<ArcanistArmorItem> ARCANIST_BOOTS = ITEMS.register("arcanist_boots",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(15)),
                    "Channeled spells charge 25% faster"));

    public static final DeferredItem<ArcanistArmorItem> WARDED_ROBE = ITEMS.register("warded_robe",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(25)).rarity(Rarity.RARE),
                    "Forbidden spells leave 25% less corruption\nTurns aside 30% of harm, paid for in mana"));

    public static final DeferredItem<ArcanistArmorItem> CROWNED_HOOD = ITEMS.register("crowned_hood",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(25)).rarity(Rarity.EPIC).fireResistant(),
                    "Spells cost 10% less mana\nCorruption cannot harm you\nMana returns twice as fast"));

    public static final DeferredItem<WellspringWandItem> WELLSPRING_WAND = ITEMS.register("wellspring_wand",
            () -> new WellspringWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<ArcanistArmorItem> SKYSTRIDER_BOOTS = ITEMS.register("skystrider_boots",
            () -> new ArcanistArmorItem(ModArmorMaterials.ARCANIST_REGALIA, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(25)).rarity(Rarity.EPIC),
                    "Channeled spells charge 25% faster\nWhile worn: double-tap jump to fly\n(2 mana each second in the air)"));

    public static final DeferredItem<Item> RESONANT_CORE =
            ITEMS.registerSimpleItem("resonant_core", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<ResonantPrismItem> RESONANT_PRISM = ITEMS.register("resonant_prism",
            () -> new ResonantPrismItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<DeferredSpawnEggItem> AMETHYST_COLOSSUS_SPAWN_EGG = ITEMS.register("amethyst_colossus_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.AMETHYST_COLOSSUS, 0x9B5DE5, 0xF0DCFF, new Item.Properties()));

    public static final DeferredItem<HolyWaterItem> HOLY_WATER = ITEMS.register("holy_water",
        () -> new HolyWaterItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SplashHolyWaterItem> SPLASH_HOLY_WATER = ITEMS.register("splash_holy_water",
        () -> new SplashHolyWaterItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<PickaxeItem> ARCANE_PICKAXE = ITEMS.register("arcane_pickaxe",
        () -> new PickaxeItem(ArcaneTools.TIER, new Item.Properties().rarity(Rarity.RARE)
            .attributes(PickaxeItem.createAttributes(ArcaneTools.TIER, 1.0f, -2.8f))));

    public static final DeferredItem<ShovelItem> ARCANE_SHOVEL = ITEMS.register("arcane_shovel",
        () -> new ShovelItem(ArcaneTools.TIER, new Item.Properties().rarity(Rarity.RARE)
            .attributes(ShovelItem.createAttributes(ArcaneTools.TIER, 1.5f, -3.0f))));

    public static final DeferredItem<AxeItem> ARCANE_AXE = ITEMS.register("arcane_axe",
        () -> new AxeItem(ArcaneTools.TIER, new Item.Properties().rarity(Rarity.RARE)
            .attributes(AxeItem.createAttributes(ArcaneTools.TIER, 5.0f, -3.0f))));

    public static final DeferredItem<DeferredSpawnEggItem> ROGUE_APPRENTICE_SPAWN_EGG = ITEMS.register("rogue_apprentice_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ROGUE_APPRENTICE, 0x5C5046, 0x784F96, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> HOLLOW_CULTIST_SPAWN_EGG = ITEMS.register("hollow_cultist_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HOLLOW_CULTIST, 0x16101A, 0xB0263F, new Item.Properties()));

    public static final DeferredItem<TornPageItem> TORN_PAGE = ITEMS.register("torn_page",
        () -> new TornPageItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<DeferredSpawnEggItem> TOWER_WIZARD_SPAWN_EGG = ITEMS.register("tower_wizard_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.TOWER_WIZARD, 0x283278, 0xDEB648, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> FIRE_WIZARD_SPAWN_EGG = ITEMS.register("fire_wizard_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.FIRE_WIZARD, 0x962216, 0xFF9628, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> FROST_WIZARD_SPAWN_EGG = ITEMS.register("frost_wizard_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.FROST_WIZARD, 0xC8E0F0, 0x6AC8F8, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> VOID_WIZARD_SPAWN_EGG = ITEMS.register("void_wizard_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.VOID_WIZARD, 0x241230, 0xAA3CDC, new Item.Properties()));

    public static final DeferredBlock<PortcullisBlock> PORTCULLIS = BLOCKS.register("portcullis",
        () -> new PortcullisBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 6.0f)
            .sound(SoundType.CHAIN).noOcclusion().requiresCorrectToolForDrops()
            .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final DeferredItem<BlockItem> PORTCULLIS_ITEM = ITEMS.registerSimpleBlockItem("portcullis", PORTCULLIS);

    public static final DeferredBlock<Block> WARDED_STONE = BLOCKS.register("warded_stone",
        () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.DEEPSLATE_BRICKS)));
    public static final DeferredItem<BlockItem> WARDED_STONE_ITEM = ITEMS.registerSimpleBlockItem("warded_stone", WARDED_STONE);

    public static final DeferredBlock<VaultDoorBlock> VAULT_DOOR = BLOCKS.register("vault_door",
        () -> new VaultDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> VAULT_DOOR_ITEM = ITEMS.registerSimpleBlockItem("vault_door", VAULT_DOOR);

    public static final DeferredItem<TreasuryKeyItem> TREASURY_KEY = ITEMS.register("treasury_key",
        () -> new TreasuryKeyItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<DeferredSpawnEggItem> CASTELLAN_SPAWN_EGG = ITEMS.register("castellan_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.CASTELLAN, 0x5A5046, 0xC8B464, new Item.Properties()));

    public static final DeferredItem<SpellTomeItem> TOME_OF_REFLECTION = ITEMS.register("tome_of_reflection",
        () -> new SpellTomeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "reflect"));

    public static final DeferredItem<DeferredSpawnEggItem> LEVIATHAN_SPAWN_EGG = ITEMS.register("leviathan_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.LEVIATHAN, 0x12223A, 0x3CAABE, new Item.Properties()));

    public static final DeferredItem<OathbreakerItem> OATHBREAKER = ITEMS.register("oathbreaker",
        () -> new OathbreakerItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<DeferredSpawnEggItem> REVENANT_KING_SPAWN_EGG = ITEMS.register("revenant_king_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.REVENANT_KING, 0x2B2730, 0xC8323C, new Item.Properties()));

    public static final DeferredBlock<ResearchTableBlock> RESEARCH_TABLE = BLOCKS.register("research_table",
            () -> new ResearchTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion()));

    public static final DeferredBlock<RitualAltarBlock> RITUAL_ALTAR = BLOCKS.register("ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().lightLevel(state -> 5)));

    public static final DeferredBlock<ManaBatteryBlock> MANA_BATTERY = BLOCKS.register("mana_battery",
            () -> new ManaBatteryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
                    .strength(3.0f, 6.0f)
                    .lightLevel(state -> state.getValue(ManaBatteryBlock.CHARGE) * 2)));
    public static final DeferredItem<BlockItem> MANA_BATTERY_ITEM = ITEMS.registerSimpleBlockItem("mana_battery", MANA_BATTERY);

    public static final DeferredBlock<ManaConduitBlock> MANA_CONDUIT = BLOCKS.register("mana_conduit",
            () -> new ManaConduitBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).strength(1.5f).noOcclusion()));
    public static final DeferredItem<BlockItem> MANA_CONDUIT_ITEM = ITEMS.registerSimpleBlockItem("mana_conduit", MANA_CONDUIT);

    public static final DeferredBlock<WardingLanternBlock> WARDING_LANTERN = BLOCKS.register("warding_lantern",
            () -> new WardingLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).noOcclusion()
                    .lightLevel(state -> state.getValue(WardingLanternBlock.LIT) ? 15 : 0)));
    public static final DeferredItem<BlockItem> WARDING_LANTERN_ITEM = ITEMS.registerSimpleBlockItem("warding_lantern", WARDING_LANTERN);

    public static final DeferredBlock<VerdantEngineBlock> VERDANT_ENGINE = BLOCKS.register("verdant_engine",
            () -> new VerdantEngineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
                    .strength(3.0f, 6.0f)
                    .lightLevel(state -> state.getValue(ManaEngineBlock.ACTIVE) ? 7 : 0)));
    public static final DeferredItem<BlockItem> VERDANT_ENGINE_ITEM = ITEMS.registerSimpleBlockItem("verdant_engine", VERDANT_ENGINE);

    public static final DeferredBlock<ResearchEngineBlock> RESEARCH_ENGINE = BLOCKS.register("research_engine",
            () -> new ResearchEngineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
                    .strength(3.0f, 6.0f)
                    .lightLevel(state -> state.getValue(ManaEngineBlock.ACTIVE) ? 7 : 0)));
    public static final DeferredItem<BlockItem> RESEARCH_ENGINE_ITEM = ITEMS.registerSimpleBlockItem("research_engine", RESEARCH_ENGINE);

    public static final DeferredBlock<ManaBeaconBlock> MANA_BEACON = BLOCKS.register("mana_beacon",
        () -> new ManaBeaconBlock(BlockBehaviour.Properties.of().noOcclusion()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 1200.0f)
            .sound(SoundType.AMETHYST)
            .lightLevel(state -> state.getValue(ManaEngineBlock.ACTIVE) ? 15 : 4)));
    public static final DeferredItem<BlockItem> MANA_BEACON_ITEM = ITEMS.registerSimpleBlockItem("mana_beacon", MANA_BEACON);

    public static final DeferredBlock<InfuserBlock> INFUSER = BLOCKS.register("infuser",
        () -> new InfuserBlock(BlockBehaviour.Properties.of().noOcclusion()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(ManaEngineBlock.ACTIVE) ? 10 : 0)));
    public static final DeferredItem<BlockItem> INFUSER_ITEM = ITEMS.registerSimpleBlockItem("infuser", INFUSER);

    public static final DeferredBlock<HollowGrassBlock> HOLLOW_GRASS = BLOCKS.register("hollow_grass",
            () -> new HollowGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE)
                    .randomTicks()
                    .sound(SoundType.MOSS)));
    public static final DeferredItem<BlockItem> HOLLOW_GRASS_ITEM = ITEMS.registerSimpleBlockItem("hollow_grass", HOLLOW_GRASS);

    public static final DeferredBlock<RotatedPillarBlock> HOLLOW_LOG = BLOCKS.register("hollow_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_STEM)));
    public static final DeferredItem<BlockItem> HOLLOW_LOG_ITEM = ITEMS.registerSimpleBlockItem("hollow_log", HOLLOW_LOG);

    public static final DeferredBlock<Block> HOLLOW_PLANKS = BLOCKS.register("hollow_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_PLANKS)));
    public static final DeferredItem<BlockItem> HOLLOW_PLANKS_ITEM = ITEMS.registerSimpleBlockItem("hollow_planks", HOLLOW_PLANKS);

    public static final DeferredBlock<LeavesBlock> HOLLOW_LEAVES = BLOCKS.register("hollow_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).lightLevel(state -> 4)));
    public static final DeferredItem<BlockItem> HOLLOW_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("hollow_leaves", HOLLOW_LEAVES);

    public static final DeferredBlock<SaplingBlock> HOLLOW_SAPLING = BLOCKS.register("hollow_sapling",
            () -> new SaplingBlock(ModTrees.HOLLOW, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
    public static final DeferredItem<BlockItem> HOLLOW_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("hollow_sapling", HOLLOW_SAPLING);

    public static final DeferredItem<BlockItem> RITUAL_ALTAR_ITEM =
            ITEMS.registerSimpleBlockItem("ritual_altar", RITUAL_ALTAR);

    public static final DeferredItem<BlockItem> RESEARCH_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem("research_table", RESEARCH_TABLE);

    public static final DeferredItem<BlockItem> GLIMMERSTONE_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("glimmerstone_ore", GLIMMERSTONE_ORE);


    public static final DeferredItem<DullShardItem> DULL_SHARD =
            ITEMS.register("dull_shard", () -> new DullShardItem(new Item.Properties()));

    // Hollowmancy's creative tab, after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> SPELLBOOK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // The journal and spellbook
                output.accept(PatchouliAPI.get().getBookStack(ResourceLocation.fromNamespaceAndPath(MODID, "arcane_journal")));
                output.accept(SPELLBOOK.get());

                // Wands and staff
                output.accept(FOCUS_WAND.get());
                output.accept(WELLSPRING_WAND.get());
                output.accept(ABYSSAL_FOCUS.get());
                output.accept(ArchmageItems.ARCHMAGE_STAFF.get());

                // Tools and weapons
                output.accept(ARCANE_PICKAXE.get());
                output.accept(ARCANE_SHOVEL.get());
                output.accept(ARCANE_AXE.get());
                output.accept(OATHBREAKER.get());

                // Arcanist's garb, then its upgrades
                output.accept(ARCANIST_HOOD.get());
                output.accept(ARCANIST_ROBE.get());
                output.accept(ARCANIST_LEGGINGS.get());
                output.accept(ARCANIST_BOOTS.get());
                output.accept(CROWNED_HOOD.get());
                output.accept(WARDED_ROBE.get());
                output.accept(SKYSTRIDER_BOOTS.get());
                output.accept(GarbUpgrades.WISPWOVEN_HOOD.get());
                output.accept(GarbUpgrades.SHADEWEAVE_ROBE.get());
                output.accept(GarbUpgrades.LURKERSCALE_LEGGINGS.get());
                output.accept(GarbUpgrades.MOTHSILK_BOOTS.get());

                // Artifacts, charms and potions
                output.accept(WARDING_AMULET.get());
                output.accept(MANA_CRYSTAL.get());
                output.accept(SOVEREIGN_CROWN.get());
                output.accept(RESONANT_PRISM.get());
                output.accept(TOME_OF_REFLECTION.get());
                output.accept(HollowMaterials.WISPBOUND_CHARM.get());
                output.accept(HollowMaterials.MOTHWING_CHARM.get());
                output.accept(HollowMaterials.HUNTSMAN_CHARM.get());
                output.accept(HollowMaterials.SHADEWEAVE_CHARM.get());
                output.accept(HollowMaterials.TIDEBOUND_CHARM.get());
                output.accept(HOLY_WATER.get());
                output.accept(SPLASH_HOLY_WATER.get());
                output.accept(HollowMaterials.MANA_TONIC.get());

                // Spell crafting: rune stone, runes, blank scroll
                SpellCraftingItems.fillCreativeTab(output);

                // Materials
                output.accept(DULL_SHARD.get());
                output.accept(VOID_SHARD.get());
                output.accept(VOID_HEART.get());
                output.accept(LOST_PAGE.get());
                output.accept(TORN_PAGE.get());
                output.accept(RESONANT_CORE.get());
                output.accept(HollowMaterials.WISP_ESSENCE.get());
                output.accept(HollowMaterials.MOTH_DUST.get());
                output.accept(HollowMaterials.ANTLER_SHARD.get());
                output.accept(HollowMaterials.SHADE_CLOTH.get());
                output.accept(HollowMaterials.LURKER_SCALE.get());

                // Boss trophies and the endgame
                output.accept(EndgameItems.SOVEREIGN_SIGIL.get());
                output.accept(EndgameItems.COLOSSUS_HEART.get());
                output.accept(EndgameItems.MORVANES_OATH.get());
                output.accept(TREASURY_KEY.get());
                output.accept(EndgameItems.SEAL_OF_UNMAKING.get());
                output.accept(ArchmageItems.SILENT_SEAL.get());
                output.accept(ArchmageItems.ECHO_HEART.get());

                // Machines
                output.accept(RESEARCH_TABLE_ITEM.get());
                output.accept(RITUAL_ALTAR_ITEM.get());
                output.accept(INFUSER_ITEM.get());
                output.accept(RESEARCH_ENGINE_ITEM.get());
                output.accept(VERDANT_ENGINE_ITEM.get());
                output.accept(MANA_BATTERY_ITEM.get());
                output.accept(MANA_CONDUIT_ITEM.get());
                output.accept(MANA_BEACON_ITEM.get());
                output.accept(WARDING_LANTERN_ITEM.get());

                // Ores
                output.accept(GLIMMERSTONE_ORE_ITEM.get());
                output.accept(VOIDSTONE_ORE_ITEM.get());

                // The Hollow's plants and wood
                output.accept(HOLLOW_GRASS_ITEM.get());
                output.accept(HOLLOW_SAPLING_ITEM.get());
                output.accept(HOLLOW_LEAVES_ITEM.get());
                output.accept(HOLLOW_LOG_ITEM.get());
                output.accept(HOLLOW_PLANKS_ITEM.get());
                HollowWood.fillCreativeTab(output);

                // Building blocks
                HollowBlocks.addToTab(output);
                output.accept(WARDED_STONE_ITEM.get());
                output.accept(PORTCULLIS_ITEM.get());
                output.accept(VAULT_DOOR_ITEM.get());

                // Spawn eggs: people, Hollow creatures, then bosses
                output.accept(TOWER_WIZARD_SPAWN_EGG.get());
                output.accept(FIRE_WIZARD_SPAWN_EGG.get());
                output.accept(FROST_WIZARD_SPAWN_EGG.get());
                output.accept(VOID_WIZARD_SPAWN_EGG.get());
                output.accept(ROGUE_APPRENTICE_SPAWN_EGG.get());
                output.accept(HOLLOW_CULTIST_SPAWN_EGG.get());
                output.accept(CastleGuards.REVENANT_GUARD_SPAWN_EGG.get());
                HollowCreatures.addToTab(output);
                output.accept(CASTELLAN_SPAWN_EGG.get());
                output.accept(LEVIATHAN_SPAWN_EGG.get());
                output.accept(REVENANT_KING_SPAWN_EGG.get());
                output.accept(AMETHYST_COLOSSUS_SPAWN_EGG.get());
                output.accept(HOLLOW_SOVEREIGN_SPAWN_EGG.get());
                output.accept(EndgameItems.LISTENER_SPAWN_EGG.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Hollowmancy(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModMenus.register(modEventBus);
        ModComponents.register(modEventBus);
        ModEntities.register(modEventBus);
        HollowFamiliars.register(modEventBus);
        CastleGuards.register(modEventBus);
        ModSounds.register(modEventBus);
        HollowmancyParticles.register(modEventBus);
        HollowCreatures.register(modEventBus);
        HollowMaterials.register(modEventBus);
        GarbUpgrades.register(modEventBus);
        HollowBlocks.register(modEventBus);
        ArchmageItems.register(modEventBus);
        ConfiguredSpreadPlacement.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        SpellCraftingItems.register(modEventBus);
        EndgameItems.register(modEventBus);
        HollowWood.init();
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, HollowmancyConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, HollowmancyExtraConfig.COMMON_SPEC, "hollowmancy-extra-common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, HollowmancyExtraConfig.CLIENT_SPEC, "hollowmancy-client.toml");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Hollowmancy's items all live in its own tab; nothing is added to the vanilla tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
