package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.walnutbeach.hollowmancy.familiar.OwlFamiliar;
import com.walnutbeach.hollowmancy.familiar.WispFamiliar;
import com.walnutbeach.hollowmancy.familiar.GolemFamiliar;

@EventBusSubscriber(modid = Hollowmancy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Hollowmancy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HollowSovereign>> HOLLOW_SOVEREIGN =
            ENTITY_TYPES.register("hollow_sovereign", () -> EntityType.Builder.of(HollowSovereign::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.9f)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("hollow_sovereign"));

    public static final DeferredHolder<EntityType<?>, EntityType<RogueApprentice>> ROGUE_APPRENTICE =
            ENTITY_TYPES.register("rogue_apprentice", () -> EntityType.Builder.of(RogueApprentice::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(8)
                    .build("rogue_apprentice"));

    public static final DeferredHolder<EntityType<?>, EntityType<HollowCultist>> HOLLOW_CULTIST =
            ENTITY_TYPES.register("hollow_cultist", () -> EntityType.Builder.of(HollowCultist::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(8)
                    .build("hollow_cultist"));

    public static final DeferredHolder<EntityType<?>, EntityType<AmethystColossus>> AMETHYST_COLOSSUS =
            ENTITY_TYPES.register("amethyst_colossus", () -> EntityType.Builder.of(AmethystColossus::new, MobCategory.MONSTER)
                    .sized(1.2f, 3.9f)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("amethyst_colossus"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownHolyWater>> HOLY_WATER =
        ENTITY_TYPES.register("holy_water", () -> EntityType.Builder.<ThrownHolyWater>of(ThrownHolyWater::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("holy_water"));

    public static final DeferredHolder<EntityType<?>, EntityType<OwlFamiliar>> OWL_FAMILIAR =
        ENTITY_TYPES.register("owl_familiar", () -> EntityType.Builder.of(OwlFamiliar::new, MobCategory.CREATURE)
            .sized(0.5f, 0.9f)
            .clientTrackingRange(10)
            .build("owl_familiar"));
    public static final DeferredHolder<EntityType<?>, EntityType<WispFamiliar>> WISP_FAMILIAR =
        ENTITY_TYPES.register("wisp_familiar", () -> EntityType.Builder.of(WispFamiliar::new, MobCategory.CREATURE)
            .sized(0.5f, 0.6f)
            .clientTrackingRange(10)
            .build("wisp_familiar"));
    public static final DeferredHolder<EntityType<?>, EntityType<GolemFamiliar>> GOLEM_FAMILIAR =
        ENTITY_TYPES.register("golem_familiar", () -> EntityType.Builder.of(GolemFamiliar::new, MobCategory.CREATURE)
            .sized(0.7f, 1.2f)
            .clientTrackingRange(10)
            .build("golem_familiar"));

    public static final DeferredHolder<EntityType<?>, EntityType<TowerWizard>> TOWER_WIZARD =
        ENTITY_TYPES.register("tower_wizard", () -> EntityType.Builder.of(TowerWizard::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(8)
            .build("tower_wizard"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireWizard>> FIRE_WIZARD =
        ENTITY_TYPES.register("fire_wizard", () -> EntityType.Builder.of(FireWizard::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(8).fireImmune()
            .build("fire_wizard"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostWizard>> FROST_WIZARD =
        ENTITY_TYPES.register("frost_wizard", () -> EntityType.Builder.of(FrostWizard::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(8)
            .build("frost_wizard"));
    public static final DeferredHolder<EntityType<?>, EntityType<VoidWizard>> VOID_WIZARD =
        ENTITY_TYPES.register("void_wizard", () -> EntityType.Builder.of(VoidWizard::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(8)
            .build("void_wizard"));

    public static final DeferredHolder<EntityType<?>, EntityType<RevenantKing>> REVENANT_KING =
        ENTITY_TYPES.register("revenant_king", () -> EntityType.Builder.of(RevenantKing::new, MobCategory.MONSTER)
            .sized(0.84f, 2.73f)
            .fireImmune()
            .clientTrackingRange(10)
            .build("revenant_king"));

    public static final DeferredHolder<EntityType<?>, EntityType<Castellan>> CASTELLAN =
        ENTITY_TYPES.register("castellan", () -> EntityType.Builder.of(Castellan::new, MobCategory.MONSTER)
            .sized(0.66f, 2.15f)
            .clientTrackingRange(10)
            .build("castellan"));

    public static final DeferredHolder<EntityType<?>, EntityType<Leviathan>> LEVIATHAN =
        ENTITY_TYPES.register("leviathan", () -> EntityType.Builder.of(Leviathan::new, MobCategory.MONSTER)
            .sized(2.97f, 2.97f)
            .eyeHeight(1.49f)
            .clientTrackingRange(10)
            .build("leviathan"));

    public static final DeferredHolder<EntityType<?>, EntityType<Listener>> LISTENER =
        ENTITY_TYPES.register("listener", () -> EntityType.Builder.of(Listener::new, MobCategory.MONSTER)
            .sized(1.2f, 4.7f)
            .fireImmune()
            .clientTrackingRange(10)
            .build("listener"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(HOLLOW_SOVEREIGN.get(), HollowSovereign.createAttributes().build());
        event.put(ROGUE_APPRENTICE.get(), RogueApprentice.createAttributes().build());
        event.put(HOLLOW_CULTIST.get(), HollowCultist.createAttributes().build());
        event.put(AMETHYST_COLOSSUS.get(), AmethystColossus.createAttributes().build());
        event.put(TOWER_WIZARD.get(), TowerWizard.createAttributes().build());
        event.put(FIRE_WIZARD.get(), TowerWizard.createAttributes().build());
        event.put(FROST_WIZARD.get(), TowerWizard.createAttributes().build());
        event.put(VOID_WIZARD.get(), TowerWizard.createAttributes().build());
        event.put(REVENANT_KING.get(), RevenantKing.createAttributes().build());
        event.put(CASTELLAN.get(), Castellan.createAttributes().build());
        event.put(LEVIATHAN.get(), Leviathan.createAttributes().build());
        event.put(LISTENER.get(), Listener.createAttributes().build());
    }

    // Natural spawns: on solid ground, in the dark, like any monster
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ROGUE_APPRENTICE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HOLLOW_CULTIST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}