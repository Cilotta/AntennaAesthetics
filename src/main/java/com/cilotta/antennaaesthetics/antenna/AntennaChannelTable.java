package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public final class AntennaChannelTable {
    private static final long STALE_AFTER_TICKS = 40L;
    private static final Map<Integer, Map<AntennaNodeKey, AntennaTransmission>> CHANNELS = new HashMap<>();

    private AntennaChannelTable() {
    }

    public static void publish(ServerLevel level, AntennaNodeKey source, int channel, Map<Identifier, AntennaPayload> payloads) {
        prune(level);
        if (payloads.isEmpty()) {
            remove(source);
            return;
        }

        CHANNELS.computeIfAbsent(channel, ignored -> new LinkedHashMap<>())
                .put(source, new AntennaTransmission(source, channel, Map.copyOf(payloads), level.getGameTime()));
    }

    public static void remove(AntennaNodeKey source) {
        CHANNELS.values().forEach(entries -> entries.remove(source));
        CHANNELS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static <T extends AntennaPayload> Optional<T> aggregate(ServerLevel level, int channel, AntennaNodeKey receiver, AntennaPayloadType<T> type) {
        prune(level);
        Collection<AntennaPayload> payloads = CHANNELS.getOrDefault(channel, Map.of()).values().stream()
                .filter(transmission -> !transmission.source().equals(receiver))
                .flatMap(transmission -> transmission.values().stream())
                .filter(payload -> payload.typeId().equals(type.id()))
                .toList();
        return type.aggregate(payloads);
    }

    public static int activeChannelCount() {
        return CHANNELS.size();
    }

    private static void prune(ServerLevel level) {
        long oldestValid = level.getGameTime() - STALE_AFTER_TICKS;
        CHANNELS.values().forEach(entries -> entries.entrySet().removeIf(entry -> entry.getValue().gameTime() < oldestValid));
        CHANNELS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
