package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.menu.AntennaBaseMenu;
import com.cilotta.antennaaesthetics.menu.RedstoneConverterMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Menu type registrations for client/server GUI synchronization.
 */
public final class ModMenus {
    /** Menu type backing the antenna base screen. */
    public static final DeferredHolder<MenuType<? extends AbstractContainerMenu>, MenuType<AntennaBaseMenu>> ANTENNA_BASE =
            AntennaAesthetics.MENUS.register("antenna_base", () -> IMenuTypeExtension.create(AntennaBaseMenu::new));
    public static final DeferredHolder<MenuType<? extends AbstractContainerMenu>, MenuType<RedstoneConverterMenu>> REDSTONE_CONVERTER =
            AntennaAesthetics.MENUS.register("redstone_converter", () -> IMenuTypeExtension.create(RedstoneConverterMenu::new));

    private ModMenus() {
    }

    /**
     * Forces static registration before deferred registers are attached.
     */
    public static void register() {
    }
}
