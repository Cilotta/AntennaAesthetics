package com.cilotta.antennaaesthetics.antenna;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

/**
 * Describes one kind of antenna payload and how multiple broadcasts of that kind
 * collapse into the value a receiver should observe.
 *
 * @param id stable payload type id
 * @param payloadClass runtime class used to filter payload instances
 * @param aggregator function that merges all in-range payloads into one value
 * @param <T> payload implementation handled by this type
 */
public record AntennaPayloadType<T extends AntennaPayload>(
        Identifier id,
        Class<T> payloadClass,
        Function<Collection<T>, Optional<T>> aggregator) {
    /**
     * Filters arbitrary payloads down to this type and applies its merge rule.
     *
     * @param payloads payloads gathered from the current channel
     * @return merged payload, or empty when no compatible payload exists
     */
    public Optional<T> aggregate(Collection<AntennaPayload> payloads) {
        return this.aggregator.apply(payloads.stream()
                .filter(this.payloadClass::isInstance)
                .map(this.payloadClass::cast)
                .toList());
    }
}
