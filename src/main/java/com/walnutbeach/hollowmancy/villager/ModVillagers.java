package com.walnutbeach.hollowmancy.villager;

import com.google.common.collect.ImmutableSet;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Hollowmancy.MODID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Hollowmancy.MODID);

    /** Marks the Research Table as a place villagers can claim as a job site. */
    public static final DeferredHolder<PoiType, PoiType> RESEARCH_TABLE_POI = POI_TYPES.register("research_table",
            () -> new PoiType(ImmutableSet.copyOf(Hollowmancy.RESEARCH_TABLE.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> ARCANIST = PROFESSIONS.register("arcanist",
            () -> new VillagerProfession("arcanist",
                    holder -> holder.value() == RESEARCH_TABLE_POI.get(),   // the job site they keep
                    holder -> holder.value() == RESEARCH_TABLE_POI.get(),   // the job site they look for
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void register(IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);
    }
}