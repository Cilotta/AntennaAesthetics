package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;

/**
 * Snapshot of one antenna's most recent broadcast on a channel.
 *
 * @param source antenna that published this transmission
 * @param channel tuned channel
 * @param range maximum block distance this antenna can reach
 * @param payloads payloads keyed by their type id
 * @param gameTime server game time when the snapshot was written
 */
public record AntennaTransmission(AntennaNodeKey source, int channel, int range, Map<Identifier, AntennaPayload> payloads, long gameTime) {
    /**
     * Returns the payload collection for stream aggregation.
     *
     * @return all payload values in this transmission
     */
    public Collection<AntennaPayload> values() {
        return this.payloads.values();
    }

    /**
     * Reads one strongly typed payload from this transmission.
     *
     * @param type requested payload type
     * @param <T> payload implementation
     * @return payload if present and type-compatible
     */
    public <T extends AntennaPayload> Optional<T> payload(AntennaPayloadType<T> type) {
        AntennaPayload payload = this.payloads.get(type.id());
        return type.payloadClass().isInstance(payload) ? Optional.of(type.payloadClass().cast(payload)) : Optional.empty();
    }
}
