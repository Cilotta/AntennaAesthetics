package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.menu.AntennaBaseMenu;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Menu type registrations for client/server GUI synchronization.
 */
public final class ModMenus {
    /** Menu type backing the antenna base screen. */
    public static final DeferredHolder<MenuType<?>, MenuType<AntennaBaseMenu>> ANTENNA_BASE =
            AntennaAesthetics.MENUS.register("antenna_base", () -> IMenuTypeExtension.create(AntennaBaseMenu::new));

    private ModMenus() {
    }

    /**
     * Forces static registration before deferred registers are attached.
     */
    public static void register() {
    }
}
