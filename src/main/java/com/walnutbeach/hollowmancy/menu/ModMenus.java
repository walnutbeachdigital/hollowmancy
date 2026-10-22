package com.walnutbeach.hollowmancy.menu;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Hollowmancy.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchMenu>> RESEARCH_TABLE =
            MENUS.register("research_table",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) -> new ResearchMenu(containerId, inventory)));

    public static final DeferredHolder<MenuType<?>, MenuType<ManaBatteryMenu>> MANA_BATTERY =
            MENUS.register("mana_battery",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) -> new ManaBatteryMenu(containerId, inventory)));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}