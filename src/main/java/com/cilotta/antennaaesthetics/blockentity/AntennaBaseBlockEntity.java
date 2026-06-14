package com.cilotta.antennaaesthetics.blockentity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.cilotta.antennaaesthetics.Config;
import com.cilotta.antennaaesthetics.antenna.AntennaChannelTable;
import com.cilotta.antennaaesthetics.antenna.AntennaFrequencyPlan;
import com.cilotta.antennaaesthetics.antenna.AntennaNodeKey;
import com.cilotta.antennaaesthetics.antenna.AntennaPayload;
import com.cilotta.antennaaesthetics.antenna.AntennaPayloadTypes;
import com.cilotta.antennaaesthetics.antenna.MusicDiscPayload;
import com.cilotta.antennaaesthetics.antenna.RedstonePayload;
import com.cilotta.antennaaesthetics.block.AntennaDataCableBlock;
import com.cilotta.antennaaesthetics.block.LinearAntennaMultiblock;
import com.cilotta.antennaaesthetics.block.LinearAntennaScanResult;
import com.cilotta.antennaaesthetics.menu.AntennaBaseMenu;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;
import org.jspecify.annotations.Nullable;

/**
 * Block entity that owns all runtime state for an antenna base.
 * <p>
 * A valid base scans its linear antenna stack, publishes local payloads to the
 * channel table, receives in-range remote payloads, drives redstone output, and
 * exposes synchronized data to the antenna UI.
 */
public class AntennaBaseBlockEntity extends BlockEntity implements MenuProvider, IMenuProviderExtension {
    /** Lowest supported tunable channel. */
    public static final int MIN_CHANNEL = AntennaFrequencyPlan.MIN_CHANNEL;
    /** Highest supported tunable channel. */
    public static final int MAX_CHANNEL = AntennaFrequencyPlan.MAX_CHANNEL;

    private int channel;
    private boolean assembled;
    private int antennaCount;
    private int receivedRedstonePower;
    private ItemStack sourceDisc = ItemStack.EMPTY;
    private final JukeboxSongPlayer remoteSongPlayer = new JukeboxSongPlayer(this::onRemoteSongChanged, this.getBlockPos());

    /**
     * Synchronized integer data consumed by {@link AntennaBaseMenu}.
     */
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AntennaBaseBlockEntity.this.assembled ? 1 : 0;
                case 1 -> AntennaBaseBlockEntity.this.antennaCount;
                case 2 -> AntennaBaseBlockEntity.this.getTransmissionRange();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return AntennaBaseMenu.DATA_COUNT;
        }
    };

    /**
     * Creates a block entity for an antenna base block.
     *
     * @param worldPosition base position
     * @param blockState current base block state
     */
    public AntennaBaseBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ANTENNA_BASE.get(), worldPosition, blockState);
    }

    /**
     * Server tick for antenna bases.
     * <p>
     * The tick refreshes the multiblock state, publishes local payloads when
     * assembled, receives remote payloads, and advances remote music playback.
     *
     * @param level current level
     * @param pos base position
     * @param state current block state
     * @param base ticking block entity
     */
    public static void tick(Level level, BlockPos pos, BlockState state, AntennaBaseBlockEntity base) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        base.updateAssemblyFromWorld();
        if (!base.assembled) {
            base.stopRemoteSong(serverLevel);
            AntennaChannelTable.remove(base.nodeKey(serverLevel));
            return;
        }

        AntennaNodeKey node = base.nodeKey(serverLevel);
        Set<RedstoneConverterBlockEntity> converters = base.connectedRedstoneConverters(serverLevel);
        AntennaChannelTable.publishAll(serverLevel, node, base.getTransmissionRange(),
                base.buildLocalTransmissions(serverLevel, converters));
        base.applyReceivedPayloads(serverLevel, node, converters);
        //base.remoteSongPlayer.tick(serverLevel, state);
    }

    /**
     * Re-scans the world and caches whether the base currently has a valid
     * linear antenna stack.
     */
    public void updateAssemblyFromWorld() {
        if (this.level == null) {
            return;
        }

        LinearAntennaScanResult result = LinearAntennaMultiblock.scanFromBase(this.level, this.worldPosition);
        this.assembled = result.valid();
        this.antennaCount = result.valid() ? result.antennaCount() : Math.max(0, result.antennaCount());
        this.channel = AntennaFrequencyPlan.nearestSupportedChannel(this.channel, this.antennaCount);
        this.setChanged();
    }

    /**
     * Performs a fresh multiblock scan for display or diagnostics.
     *
     * @return current scan result, or a missing-level failure result
     */
    public LinearAntennaScanResult currentScan() {
        return this.level == null
                ? LinearAntennaScanResult.missingLevel(this.worldPosition)
                : LinearAntennaMultiblock.scanFromBase(this.level, this.worldPosition);
    }

    /**
     * Returns the cached number of antenna segments.
     *
     * @return antenna count from the latest scan
     */
    public int getAntennaCount() {
        return this.antennaCount;
    }

    /**
     * Reports whether the latest scan produced a valid structure.
     *
     * @return true when assembled
     */
    public boolean isAssembled() {
        return this.assembled;
    }

    /**
     * Returns redstone power received from remote antennas.
     *
     * @return redstone output power from 0 to 15
     */
    public int getReceivedRedstonePower() {
        return this.receivedRedstonePower;
    }

    /**
     * Computes the current transmission range from the latest antenna count.
     *
     * @return range in blocks, or zero when invalid
     */
    public int getTransmissionRange() {
        return this.assembled ? this.antennaCount * LinearAntennaMultiblock.blocksPerAntenna() : 0;
    }

    /**
     * Stores a music disc stack as this base's local audio source.
     *
     * @param stack disc stack to broadcast
     */
    public void setSourceDisc(ItemStack stack) {
        this.sourceDisc = stack.copyWithCount(1);
        this.setChanged();
    }

    /**
     * Clears the local music-disc broadcast source.
     */
    public void clearSourceDisc() {
        this.sourceDisc = ItemStack.EMPTY;
        this.setChanged();
    }

    /**
     * Reports whether the base has a local disc source.
     *
     * @return true when a disc source is stored
     */
    public boolean hasSourceDisc() {
        return !this.sourceDisc.isEmpty();
    }

    /**
     * Removes this base's node from all channel table entries.
     */
    public void removeFromChannel() {
        if (this.level instanceof ServerLevel serverLevel) {
            AntennaChannelTable.remove(this.nodeKey(serverLevel));
        }
    }

    /**
     * Builds the payload map this base publishes this tick.
     *
     * @param level server level used for redstone lookup and song resolution
     * @return payloads keyed by payload type id
     */
    private Map<Integer, Map<Identifier, AntennaPayload>> buildLocalTransmissions(ServerLevel level,
            Set<RedstoneConverterBlockEntity> converters) {
        Set<Integer> supportedChannels = Set.copyOf(AntennaFrequencyPlan.supportedChannels(this.antennaCount));
        Map<Integer, Integer> redstoneByChannel = new HashMap<>();
        for (RedstoneConverterBlockEntity converter : converters) {
            int channel = converter.getInputChannel();
            if (!supportedChannels.contains(channel)) {
                continue;
            }
            int power = converter.getLocalInputPower(level);
            if (power > 0) {
                redstoneByChannel.merge(channel, power, Math::max);
            }
        }

        Map<Integer, Map<Identifier, AntennaPayload>> transmissions = new HashMap<>();
        redstoneByChannel.forEach((channel, power) -> transmissions
                .computeIfAbsent(channel, ignored -> new HashMap<>())
                .put(RedstonePayload.ID, new RedstonePayload(power)));

        if (supportedChannels.contains(this.channel)) {
            JukeboxSong.fromStack(this.sourceDisc).ifPresent(song -> transmissions
                    .computeIfAbsent(this.channel, ignored -> new HashMap<>())
                    .put(MusicDiscPayload.ID, new MusicDiscPayload(this.sourceDisc, song)));
        }
        return transmissions;
    }

    /**
     * Finds redstone converters adjacent to this base or connected through a
     * graph of antenna data cable blocks.
     * <p>
     * The scan is distance-limited by config so large cable networks cannot
     * create unbounded per-tick work. Converters are terminals: they are used as
     * signal endpoints but are not walked through, which keeps multiple
     * converter clusters from accidentally cross-feeding each other.
     *
     * @param level server level containing the cable graph
     * @return connected converter block entities
     */
    private Set<RedstoneConverterBlockEntity> connectedRedstoneConverters(ServerLevel level) {
        Set<RedstoneConverterBlockEntity> converters = new HashSet<>();
        Set<BlockPos> visitedCables = new HashSet<>();
        Map<BlockPos, Integer> distances = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            this.collectCableOrConverter(level, neighborPos, 1, visitedCables, distances, queue, converters);
        }

        int maxDistance = Config.DATA_CABLE_SCAN_RANGE.getAsInt();
        while (!queue.isEmpty()) {
            BlockPos cablePos = queue.removeFirst();
            int nextDistance = distances.getOrDefault(cablePos, 0) + 1;
            if (nextDistance > maxDistance) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = cablePos.relative(direction);
                this.collectCableOrConverter(level, neighborPos, nextDistance, visitedCables, distances, queue, converters);
            }
        }

        return converters;
    }

    /**
     * Adds one cable or converter candidate to the data-cable scan state.
     *
     * @param level server level containing the candidate
     * @param pos candidate position
     * @param distance graph distance from the base
     * @param visitedCables cable positions already queued
     * @param distances known cable distances
     * @param queue pending cable positions
     * @param converters discovered converter endpoints
     */
    private void collectCableOrConverter(ServerLevel level, BlockPos pos, int distance, Set<BlockPos> visitedCables, Map<BlockPos, Integer> distances,
            ArrayDeque<BlockPos> queue, Set<RedstoneConverterBlockEntity> converters) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof AntennaDataCableBlock) {
            BlockPos immutablePos = pos.immutable();
            if (visitedCables.add(immutablePos)) {
                distances.put(immutablePos, distance);
                queue.addLast(immutablePos);
            }
            return;
        }

        if (level.getBlockEntity(pos) instanceof RedstoneConverterBlockEntity converter) {
            converters.add(converter);
        }
    }

    /**
     * Reads remote payloads visible to this base and applies their effects.
     *
     * @param level server level
     * @param node this base's channel table identity
     */
    private void applyReceivedPayloads(ServerLevel level, AntennaNodeKey node,
            Set<RedstoneConverterBlockEntity> converters) {
        Set<Integer> supportedChannels = Set.copyOf(AntennaFrequencyPlan.supportedChannels(this.antennaCount));
        int nextPower = 0;
        for (RedstoneConverterBlockEntity converter : converters) {
            int channel = converter.getOutputChannel();
            int converterPower = supportedChannels.contains(channel)
                    ? AntennaChannelTable.aggregate(level, channel, node, this.getTransmissionRange(), AntennaPayloadTypes.REDSTONE)
                            .map(RedstonePayload::power)
                            .orElse(0)
                    : 0;
            converter.setReceivedPower(level, converterPower);
            nextPower = Math.max(nextPower, converterPower);
        }
        if (nextPower != this.receivedRedstonePower) {
            this.receivedRedstonePower = nextPower;
            level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.setChanged();
        }

        Optional<MusicDiscPayload> musicPayload = AntennaChannelTable.aggregate(level, this.channel, node, this.getTransmissionRange(), AntennaPayloadTypes.MUSIC_DISC);
        if (musicPayload.isPresent()) {
            MusicDiscPayload payload = musicPayload.get();
            JukeboxSong currentSong = this.remoteSongPlayer.getSong();
            if (currentSong != payload.song().value()) {
                this.remoteSongPlayer.play(level, payload.song());
            }
        } else {
            this.stopRemoteSong(level);
        }
    }

    /**
     * Stops remote song playback if one is active.
     *
     * @param level server level
     */
    private void stopRemoteSong(ServerLevel level) {
        this.remoteSongPlayer.stop(level, this.getBlockState());
    }

    /**
     * Notifies redstone neighbors and marks data dirty when remote music state
     * changes.
     */
    private void onRemoteSongChanged() {
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.setChanged();
        }
    }

    /**
     * Creates the stable node key used by the global channel table.
     *
     * @param level server level containing this base
     * @return node key for this base
     */
    private AntennaNodeKey nodeKey(ServerLevel level) {
        return new AntennaNodeKey(level.dimension(), this.worldPosition);
    }

    /**
     * Loads persisted antenna state from NBT-like value input.
     *
     * @param input serialized value input
     */
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.channel = input.getIntOr("channel", MIN_CHANNEL);
        this.assembled = input.getBooleanOr("assembled", false);
        this.antennaCount = input.getIntOr("antenna_count", 0);
        this.receivedRedstonePower = input.getIntOr("received_redstone_power", 0);
        this.sourceDisc = input.read("source_disc", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /**
     * Saves persistent antenna state.
     *
     * @param output serialized value output
     */
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("channel", this.channel);
        output.putBoolean("assembled", this.assembled);
        output.putInt("antenna_count", this.antennaCount);
        output.putInt("received_redstone_power", this.receivedRedstonePower);
        if (!this.sourceDisc.isEmpty()) {
            output.store("source_disc", ItemStack.CODEC, this.sourceDisc);
        }
    }

    /**
     * Returns the title used by the antenna base menu.
     *
     * @return translated display name
     */
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.antennaaesthetics.antenna_base");
    }

    /**
     * Creates the server-side menu instance when a player opens the base.
     *
     * @param containerId container id assigned by Minecraft
     * @param playerInventory opening player's inventory
     * @param player opening player
     * @return antenna base menu
     */
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        this.updateAssemblyFromWorld();
        return new AntennaBaseMenu(containerId, playerInventory, this, this.menuData);
    }

    /**
     * Writes the base position to the client so the client-side menu can create
     * a matching access object.
     *
     * @param menu menu being opened
     * @param buffer network buffer for extra screen data
     */
    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
    }
}
