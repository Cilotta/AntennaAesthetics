package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/**
 * Server-side global table of active antenna broadcasts.
 * <p>
 * Each antenna base publishes short-lived snapshots into a channel. Receivers
 * aggregate compatible payloads from other bases on the same channel when both
 * antennas are in the same dimension and within mutual range.
 */
public final class AntennaChannelTable {
    /** Time after which an antenna broadcast is considered stale. */
    private static final long STALE_AFTER_TICKS = 40L;
    /** Channel id to antenna node transmissions. */
    private static final Map<Integer, Map<AntennaNodeKey, AntennaTransmission>> CHANNELS = new HashMap<>();

    private AntennaChannelTable() {
    }

    /**
     * Publishes the current payloads for one antenna base.
     *
     * @param level server level used for game time pruning
     * @param source antenna node identity
     * @param channel tuned channel
     * @param range transmission range in blocks
     * @param payloads payloads produced by the base this tick
     */
    public static void publish(ServerLevel level, AntennaNodeKey source, int channel, int range, Map<Identifier, AntennaPayload> payloads) {
        prune(level);
        if (payloads.isEmpty()) {
            remove(source);
            return;
        }

        CHANNELS.computeIfAbsent(channel, ignored -> new LinkedHashMap<>())
                .put(source, new AntennaTransmission(source, channel, range, Map.copyOf(payloads), level.getGameTime()));
    }

    /**
     * Atomically replaces every channel currently published by one antenna.
     */
    public static void publishAll(ServerLevel level, AntennaNodeKey source, int range,
            Map<Integer, Map<Identifier, AntennaPayload>> transmissions) {
        prune(level);
        remove(source);
        transmissions.forEach((channel, payloads) -> {
            if (!payloads.isEmpty()) {
                CHANNELS.computeIfAbsent(channel, ignored -> new LinkedHashMap<>())
                        .put(source, new AntennaTransmission(source, channel, range, Map.copyOf(payloads),
                                level.getGameTime()));
            }
        });
    }

    /**
     * Removes one antenna from every channel.
     *
     * @param source node to remove, usually when broken or invalid
     */
    public static void remove(AntennaNodeKey source) {
        CHANNELS.values().forEach(entries -> entries.remove(source));
        CHANNELS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Aggregates all in-range payloads of a given type for a receiver.
     *
     * @param level server level used for pruning
     * @param channel receiver channel
     * @param receiver receiving antenna node
     * @param receiverRange receiver range in blocks
     * @param type payload type to aggregate
     * @param <T> payload implementation
     * @return merged payload visible to the receiver
     */
    public static <T extends AntennaPayload> Optional<T> aggregate(ServerLevel level, int channel, AntennaNodeKey receiver, int receiverRange, AntennaPayloadType<T> type) {
        prune(level);
        Collection<AntennaPayload> payloads = CHANNELS.getOrDefault(channel, Map.of()).values().stream()
                .filter(transmission -> !transmission.source().equals(receiver))
                .filter(transmission -> canReach(transmission, receiver, receiverRange))
                .flatMap(transmission -> transmission.values().stream())
                .filter(payload -> payload.typeId().equals(type.id()))
                .toList();
        return type.aggregate(payloads);
    }

    /**
     * Returns the number of currently active channels.
     *
     * @return active channel count after the latest prune/publish operation
     */
    public static int activeChannelCount() {
        return CHANNELS.size();
    }

    /**
     * Deletes old transmissions so unloaded or broken bases stop broadcasting.
     *
     * @param level level whose game time defines staleness
     */
    private static void prune(ServerLevel level) {
        long oldestValid = level.getGameTime() - STALE_AFTER_TICKS;
        CHANNELS.values().forEach(entries -> entries.entrySet().removeIf(entry -> entry.getValue().gameTime() < oldestValid));
        CHANNELS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Checks same-dimension and mutual-range reachability.
     *
     * @param transmission sender transmission
     * @param receiver receiver node
     * @param receiverRange receiver range in blocks
     * @return true when the two antennas can exchange payloads
     */
    private static boolean canReach(AntennaTransmission transmission, AntennaNodeKey receiver, int receiverRange) {
        if (!transmission.source().dimension().equals(receiver.dimension())) {
            return false;
        }

        int range = Math.min(transmission.range(), receiverRange);
        return transmission.source().pos().distSqr(receiver.pos()) <= (double) range * range;
    }
}
