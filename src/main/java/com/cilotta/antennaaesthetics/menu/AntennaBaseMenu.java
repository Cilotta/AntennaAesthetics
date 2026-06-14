package com.cilotta.antennaaesthetics.menu;

import com.cilotta.antennaaesthetics.blockentity.AntennaBaseBlockEntity;
import com.cilotta.antennaaesthetics.registry.ModBlocks;
import com.cilotta.antennaaesthetics.registry.ModMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Container menu for the antenna base screen.
 * <p>
 * This menu has no item slots. It only synchronizes base telemetry through
 * {@link ContainerData}.
 */
public class AntennaBaseMenu extends AbstractContainerMenu {
    /** Number of synchronized integer data slots. */
    public static final int DATA_COUNT = 3;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /**
     * Client-side menu constructor. The base block position is sent by the
     * server through {@link net.neoforged.neoforge.common.extensions.IMenuProviderExtension}.
     *
     * @param containerId container id assigned by Minecraft
     * @param inventory player inventory
     * @param buffer extra open-screen data
     */
    public AntennaBaseMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getClientAccess(inventory.player.level(), buffer.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    /**
     * Server-side menu constructor bound to a real block entity.
     *
     * @param containerId container id assigned by Minecraft
     * @param inventory player inventory
     * @param base antenna base block entity
     * @param data synchronized data view
     */
    public AntennaBaseMenu(int containerId, Inventory inventory, AntennaBaseBlockEntity base, ContainerData data) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), base.getBlockPos()), data);
    }

    /**
     * Shared constructor used by both client and server menu instances.
     *
     * @param containerId container id assigned by Minecraft
     * @param inventory player inventory
     * @param access position access for validity checks
     * @param data synchronized data view
     */
    private AntennaBaseMenu(int containerId, Inventory inventory, ContainerLevelAccess access, ContainerData data) {
        super(ModMenus.ANTENNA_BASE.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.access = access;
        this.data = data;
        this.addDataSlots(data);
    }

    /**
     * Keeps the menu open only while the player remains near an antenna base.
     *
     * @param player viewing player
     * @return true when the menu should stay open
     */
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ANTENNA_BASE.get());
    }

    /**
     * No-op because this menu does not expose item slots.
     *
     * @param player player attempting quick move
     * @param slotIndex slot index
     * @return empty stack
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    /**
     * Returns whether the base is currently assembled.
     *
     * @return true when assembled
     */
    public boolean assembled() {
        return this.data.get(0) == 1;
    }

    /**
     * Returns synchronized antenna segment count.
     *
     * @return antenna count
     */
    public int antennaCount() {
        return this.data.get(1);
    }

    /**
     * Returns synchronized transmission range.
     *
     * @return range in blocks
     */
    public int range() {
        return this.data.get(2);
    }

    /**
     * Creates a client-side access object for base validity checks.
     *
     * @param level client level
     * @param pos base position
     * @return container level access
     */
    private static ContainerLevelAccess getClientAccess(Level level, BlockPos pos) {
        return ContainerLevelAccess.create(level, pos);
    }
}
