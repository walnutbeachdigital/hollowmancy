package com.walnutbeach.hollowmancy.block;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class ModTrees {

    public static final TreeGrower HOLLOW = new TreeGrower(
            "hollowmancy_hollow",
            Optional.empty(),   // no 2x2 "mega" version
            Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "hollow_tree"))),
            Optional.empty());  // no flowering version
}