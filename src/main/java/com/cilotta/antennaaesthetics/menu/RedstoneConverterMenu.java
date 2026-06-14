package com.cilotta.antennaaesthetics.menu;

import com.cilotta.antennaaesthetics.blockentity.RedstoneConverterBlockEntity;
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

/** Menu used to configure one redstone converter endpoint. */
public class RedstoneConverterMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 3;
    public static final int BUTTON_INPUT_DOWN = 0;
    public static final int BUTTON_INPUT_UP = 1;
    public static final int BUTTON_OUTPUT_DOWN = 2;
    public static final int BUTTON_OUTPUT_UP = 3;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final @Nullable RedstoneConverterBlockEntity converter;

    public RedstoneConverterMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getClientAccess(inventory.player.level(), buffer.readBlockPos()), null,
                new SimpleContainerData(DATA_COUNT));
    }

    public RedstoneConverterMenu(int containerId, Inventory inventory, RedstoneConverterBlockEntity converter,
            ContainerData data) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), converter.getBlockPos()),
                converter, data);
    }

    private RedstoneConverterMenu(int containerId, Inventory inventory, ContainerLevelAccess access,
            @Nullable RedstoneConverterBlockEntity converter, ContainerData data) {
        super(ModMenus.REDSTONE_CONVERTER.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.access = access;
        this.converter = converter;
        this.data = data;
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.REDSTONE_CONVERTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.converter == null) {
            return false;
        }
        return switch (buttonId) {
            case BUTTON_INPUT_DOWN -> this.converter.adjustInputChannel(-1);
            case BUTTON_INPUT_UP -> this.converter.adjustInputChannel(1);
            case BUTTON_OUTPUT_DOWN -> this.converter.adjustOutputChannel(-1);
            case BUTTON_OUTPUT_UP -> this.converter.adjustOutputChannel(1);
            default -> false;
        };
    }

    public int inputChannel() {
        return this.data.get(0);
    }

    public int outputChannel() {
        return this.data.get(1);
    }

    public int receivedPower() {
        return this.data.get(2);
    }

    private static ContainerLevelAccess getClientAccess(Level level, BlockPos pos) {
        return ContainerLevelAccess.create(level, pos);
    }
}
