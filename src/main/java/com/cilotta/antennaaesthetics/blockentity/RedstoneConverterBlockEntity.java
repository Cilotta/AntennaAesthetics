package com.cilotta.antennaaesthetics.blockentity;

import com.cilotta.antennaaesthetics.block.AntennaBaseBlock;
import com.cilotta.antennaaesthetics.block.AntennaDataCableBlock;
import com.cilotta.antennaaesthetics.block.RedstoneConverterBlock;
import com.cilotta.antennaaesthetics.menu.RedstoneConverterMenu;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;
import org.jspecify.annotations.Nullable;

/**
 * Runtime state for a redstone converter.
 * <p>
 * The converter has two sides conceptually: it reads external vanilla redstone
 * as local antenna input, and it emits remote antenna redstone as vanilla
 * redstone output. It ignores antenna infrastructure blocks while reading input
 * to avoid feedback loops.
 */
public class RedstoneConverterBlockEntity extends BlockEntity implements MenuProvider, IMenuProviderExtension {
    public static final int DISABLED_CHANNEL = -1;
    public static final int MAX_CHANNEL = 99;

    private int inputChannel = 8;
    private int outputChannel = 8;
    private int receivedPower;
    private long lastBaseUpdateTick = Long.MIN_VALUE;
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> RedstoneConverterBlockEntity.this.inputChannel;
                case 1 -> RedstoneConverterBlockEntity.this.outputChannel;
                case 2 -> RedstoneConverterBlockEntity.this.receivedPower;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return RedstoneConverterMenu.DATA_COUNT;
        }
    };

    /**
     * Creates a redstone converter block entity.
     *
     * @param worldPosition converter position
     * @param blockState converter block state
     */
    public RedstoneConverterBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.REDSTONE_CONVERTER.get(), worldPosition, blockState);
    }

    /**
     * Clears stale remote output when no antenna base updates this converter.
     *
     * @param level current level
     * @param pos converter position
     * @param state converter state
     * @param converter ticking converter
     */
    public static void tick(Level level, BlockPos pos, BlockState state, RedstoneConverterBlockEntity converter) {
        if (level instanceof ServerLevel serverLevel
                && converter.receivedPower > 0
                && serverLevel.getGameTime() - converter.lastBaseUpdateTick > 2L) {
            converter.setReceivedPower(serverLevel, 0);
        }
    }

    /**
     * Returns the redstone power this converter is emitting from remote input.
     *
     * @return received power from 0 to 15
     */
    public int getReceivedPower() {
        return this.receivedPower;
    }

    public int getInputChannel() {
        return this.inputChannel;
    }

    public int getOutputChannel() {
        return this.outputChannel;
    }

    public boolean adjustInputChannel(int delta) {
        this.inputChannel = cycleChannel(this.inputChannel, delta);
        this.setChanged();
        return true;
    }

    public boolean adjustOutputChannel(int delta) {
        this.outputChannel = cycleChannel(this.outputChannel, delta);
        this.setChanged();
        return true;
    }

    private static int cycleChannel(int channel, int delta) {
        return Math.floorMod(channel + 1 + delta, MAX_CHANNEL + 2) - 1;
    }

    /**
     * Reads local vanilla redstone input while avoiding antenna infrastructure.
     *
     * @param level server level used for redstone lookup
     * @return strongest external redstone input
     */
    public int getLocalInputPower(ServerLevel level) {
        if (this.receivedPower > 0 && this.inputChannel == this.outputChannel) {
            return 0;
        }

        int power = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof AntennaBaseBlock
                    || neighborState.getBlock() instanceof AntennaDataCableBlock
                    || neighborState.getBlock() instanceof RedstoneConverterBlock) {
                continue;
            }

            power = Math.max(power, level.getSignal(neighborPos, direction));
            if (power >= 15) {
                return 15;
            }
        }
        return power;
    }

    /**
     * Applies remote antenna power to this converter's vanilla redstone output.
     *
     * @param level server level
     * @param power new received power
     */
    public void setReceivedPower(ServerLevel level, int power) {
        int clamped = Math.clamp(power, 0, 15);
        this.lastBaseUpdateTick = level.getGameTime();
        if (clamped != this.receivedPower) {
            this.receivedPower = clamped;
            level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.setChanged();
        }
    }

    /**
     * Loads persisted converter output state.
     *
     * @param input serialized value input
     */
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inputChannel = input.getIntOr("input_channel", 8);
        this.outputChannel = input.getIntOr("output_channel", 8);
        this.receivedPower = input.getIntOr("received_power", 0);
    }

    /**
     * Saves converter output state.
     *
     * @param output serialized value output
     */
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("input_channel", this.inputChannel);
        output.putInt("output_channel", this.outputChannel);
        output.putInt("received_power", this.receivedPower);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.antennaaesthetics.redstone_converter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RedstoneConverterMenu(containerId, inventory, this, this.menuData);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
    }
}
