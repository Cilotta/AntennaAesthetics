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
import org.jspecify.annotations.Nullable;

/**
 * Container menu for the antenna base screen.
 * <p>
 * This menu has no item slots. It only synchronizes base telemetry through
 * {@link ContainerData} and routes channel button clicks back to the server.
 */
public class AntennaBaseMenu extends AbstractContainerMenu {
    /** Number of synchronized integer data slots. */
    public static final int DATA_COUNT = 7;
    /** Button id for decrementing the channel. */
    public static final int BUTTON_CHANNEL_DOWN = 0;
    /** Button id for incrementing the channel. */
    public static final int BUTTON_CHANNEL_UP = 1;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final @Nullable AntennaBaseBlockEntity base;

    /**
     * Client-side menu constructor. The base block position is sent by the
     * server through {@link net.neoforged.neoforge.common.extensions.IMenuProviderExtension}.
     *
     * @param containerId container id assigned by Minecraft
     * @param inventory player inventory
     * @param buffer extra open-screen data
     */
    public AntennaBaseMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getClientAccess(inventory.player.level(), buffer.readBlockPos()), null, new SimpleContainerData(DATA_COUNT));
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
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), base.getBlockPos()), base, data);
    }

    /**
     * Shared constructor used by both client and server menu instances.
     *
     * @param containerId container id assigned by Minecraft
     * @param inventory player inventory
     * @param access position access for validity checks
     * @param base server-side base, or null on the client
     * @param data synchronized data view
     */
    private AntennaBaseMenu(int containerId, Inventory inventory, ContainerLevelAccess access, @Nullable AntennaBaseBlockEntity base, ContainerData data) {
        super(ModMenus.ANTENNA_BASE.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.access = access;
        this.base = base;
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
     * Handles channel adjustment button packets from the client.
     *
     * @param player player pressing the button
     * @param buttonId button id
     * @return true when handled
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.base == null) {
            return false;
        }

        if (buttonId == BUTTON_CHANNEL_DOWN) {
            this.base.adjustChannel(-1);
            return true;
        }

        if (buttonId == BUTTON_CHANNEL_UP) {
            this.base.adjustChannel(1);
            return true;
        }

        return false;
    }

    /**
     * Returns the synchronized channel.
     *
     * @return channel number
     */
    public int channel() {
        return this.data.get(0);
    }

    /**
     * Returns whether the base is currently assembled.
     *
     * @return true when assembled
     */
    public boolean assembled() {
        return this.data.get(1) == 1;
    }

    /**
     * Returns synchronized antenna segment count.
     *
     * @return antenna count
     */
    public int antennaCount() {
        return this.data.get(2);
    }

    /**
     * Returns synchronized received redstone power.
     *
     * @return redstone power from 0 to 15
     */
    public int redstonePower() {
        return this.data.get(3);
    }

    /**
     * Returns synchronized transmission range.
     *
     * @return range in blocks
     */
    public int range() {
        return this.data.get(4);
    }

    /**
     * Returns configured maximum antenna count.
     *
     * @return maximum antenna count
     */
    public int maxAntennaCount() {
        return this.data.get(5);
    }

    /**
     * Returns configured range contribution per antenna segment.
     *
     * @return blocks of range per segment
     */
    public int blocksPerAntenna() {
        return this.data.get(6);
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
