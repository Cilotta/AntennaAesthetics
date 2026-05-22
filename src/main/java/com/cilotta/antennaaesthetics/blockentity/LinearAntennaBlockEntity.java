package com.cilotta.antennaaesthetics.blockentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.cilotta.antennaaesthetics.antenna.AntennaChannelTable;
import com.cilotta.antennaaesthetics.antenna.AntennaNodeKey;
import com.cilotta.antennaaesthetics.antenna.AntennaPayload;
import com.cilotta.antennaaesthetics.antenna.AntennaPayloadTypes;
import com.cilotta.antennaaesthetics.antenna.MusicDiscPayload;
import com.cilotta.antennaaesthetics.antenna.RedstonePayload;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LinearAntennaBlockEntity extends BlockEntity {
    public static final int MIN_CHANNEL = 0;
    public static final int MAX_CHANNEL = 99;

    private int channel;
    private boolean assembled;
    private BlockPos controllerPos = BlockPos.ZERO;
    private int elementCount;
    private int receivedRedstonePower;
    private ItemStack sourceDisc = ItemStack.EMPTY;
    private final JukeboxSongPlayer remoteSongPlayer = new JukeboxSongPlayer(this::onRemoteSongChanged, this.getBlockPos());

    public LinearAntennaBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.LINEAR_ANTENNA.get(), worldPosition, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LinearAntennaBlockEntity antenna) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!antenna.assembled) {
            antenna.stopRemoteSong(serverLevel);
            AntennaChannelTable.remove(antenna.nodeKey(serverLevel));
            return;
        }

        // The controller owns the shared antenna node, so element ticks cannot overwrite each other.
        if (!antenna.isController()) {
            return;
        }

        AntennaNodeKey node = antenna.nodeKey(serverLevel);
        AntennaChannelTable.publish(serverLevel, node, antenna.channel, antenna.buildLocalPayloads(serverLevel, pos));
        antenna.applyReceivedPayloads(serverLevel, node);
        antenna.remoteSongPlayer.tick(serverLevel, state);
    }

    public int getChannel() {
        return this.channel;
    }

    public void setChannel(int channel) {
        this.channel = Math.clamp(channel, MIN_CHANNEL, MAX_CHANNEL);
        this.setChanged();
    }

    public int cycleChannel() {
        this.channel = this.channel >= MAX_CHANNEL ? MIN_CHANNEL : this.channel + 1;
        this.setChanged();
        return this.channel;
    }

    public boolean isAssembled() {
        return this.assembled;
    }

    public int getElementCount() {
        return this.elementCount;
    }

    public int getReceivedRedstonePower() {
        return this.receivedRedstonePower;
    }

    public void setAssembly(BlockPos controllerPos, int elementCount, boolean assembled) {
        this.controllerPos = controllerPos.immutable();
        this.elementCount = assembled ? elementCount : 0;
        this.assembled = assembled;
        this.setChanged();
    }

    public void setSourceDisc(ItemStack stack) {
        this.sourceDisc = stack.copyWithCount(1);
        this.setChanged();
    }

    public void clearSourceDisc() {
        this.sourceDisc = ItemStack.EMPTY;
        this.setChanged();
    }

    public boolean hasSourceDisc() {
        return !this.sourceDisc.isEmpty();
    }

    public void removeFromChannel() {
        if (this.level instanceof ServerLevel serverLevel) {
            AntennaChannelTable.remove(this.nodeKey(serverLevel));
        }
    }

    private Map<Identifier, AntennaPayload> buildLocalPayloads(ServerLevel level, BlockPos pos) {
        Map<Identifier, AntennaPayload> payloads = new HashMap<>();
        int localPower = 0;
        ItemStack disc = ItemStack.EMPTY;

        for (BlockPos elementPos = this.controllerPos; elementPos.getY() < this.controllerPos.getY() + this.elementCount; elementPos = elementPos.above()) {
            localPower = Math.max(localPower, level.getBestNeighborSignal(elementPos));
            if (disc.isEmpty() && level.getBlockEntity(elementPos) instanceof LinearAntennaBlockEntity element && element.hasSourceDisc()) {
                disc = element.sourceDisc;
            }
        }

        if (localPower > 0) {
            payloads.put(RedstonePayload.ID, new RedstonePayload(localPower));
        }

        ItemStack discToBroadcast = disc;
        JukeboxSong.fromStack(discToBroadcast)
                .ifPresent(song -> payloads.put(MusicDiscPayload.ID, new MusicDiscPayload(discToBroadcast, song)));
        return payloads;
    }

    private void applyReceivedPayloads(ServerLevel level, AntennaNodeKey node) {
        int nextPower = AntennaChannelTable.aggregate(level, this.channel, node, AntennaPayloadTypes.REDSTONE)
                .map(RedstonePayload::power)
                .orElse(0);
        if (nextPower != this.receivedRedstonePower) {
            this.setReceivedRedstonePower(level, nextPower);
        }

        Optional<MusicDiscPayload> musicPayload = AntennaChannelTable.aggregate(level, this.channel, node, AntennaPayloadTypes.MUSIC_DISC);
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

    private void stopRemoteSong(ServerLevel level) {
        this.remoteSongPlayer.stop(level, this.getBlockState());
    }

    private void onRemoteSongChanged() {
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.setChanged();
        }
    }

    private AntennaNodeKey nodeKey(ServerLevel level) {
        return new AntennaNodeKey(level.dimension(), this.assembled ? this.controllerPos : this.worldPosition);
    }

    private void setReceivedRedstonePower(ServerLevel level, int power) {
        for (BlockPos elementPos = this.controllerPos; elementPos.getY() < this.controllerPos.getY() + this.elementCount; elementPos = elementPos.above()) {
            if (level.getBlockEntity(elementPos) instanceof LinearAntennaBlockEntity element) {
                element.receivedRedstonePower = power;
                level.updateNeighborsAt(elementPos, element.getBlockState().getBlock());
                element.setChanged();
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.channel = input.getIntOr("channel", MIN_CHANNEL);
        this.assembled = input.getBooleanOr("assembled", false);
        this.elementCount = input.getIntOr("element_count", 0);
        int controllerX = input.getIntOr("controller_x", this.worldPosition.getX());
        int controllerY = input.getIntOr("controller_y", this.worldPosition.getY());
        int controllerZ = input.getIntOr("controller_z", this.worldPosition.getZ());
        this.controllerPos = new BlockPos(controllerX, controllerY, controllerZ);
        this.receivedRedstonePower = input.getIntOr("received_redstone_power", 0);
        this.sourceDisc = input.read("source_disc", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("channel", this.channel);
        output.putBoolean("assembled", this.assembled);
        output.putInt("element_count", this.elementCount);
        output.putInt("controller_x", this.controllerPos.getX());
        output.putInt("controller_y", this.controllerPos.getY());
        output.putInt("controller_z", this.controllerPos.getZ());
        output.putInt("received_redstone_power", this.receivedRedstonePower);
        if (!this.sourceDisc.isEmpty()) {
            output.store("source_disc", ItemStack.CODEC, this.sourceDisc);
        }
    }

    public boolean isController() {
        return this.assembled && this.controllerPos.equals(this.worldPosition);
    }
}
