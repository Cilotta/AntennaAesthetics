package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;

public record AntennaTransmission(AntennaNodeKey source, int channel, Map<Identifier, AntennaPayload> payloads, long gameTime) {
    public Collection<AntennaPayload> values() {
        return this.payloads.values();
    }

    public <T extends AntennaPayload> Optional<T> payload(AntennaPayloadType<T> type) {
        AntennaPayload payload = this.payloads.get(type.id());
        return type.payloadClass().isInstance(payload) ? Optional.of(type.payloadClass().cast(payload)) : Optional.empty();
    }
}
