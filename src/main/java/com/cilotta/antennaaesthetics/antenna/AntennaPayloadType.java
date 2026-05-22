package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

public record AntennaPayloadType<T extends AntennaPayload>(
        Identifier id,
        Class<T> payloadClass,
        Function<Collection<T>, Optional<T>> aggregator) {
    public Optional<T> aggregate(Collection<AntennaPayload> payloads) {
        return this.aggregator.apply(payloads.stream()
                .filter(this.payloadClass::isInstance)
                .map(this.payloadClass::cast)
                .toList());
    }
}
