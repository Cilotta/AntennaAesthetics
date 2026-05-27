package com.cilotta.antennaaesthetics.antenna;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Built-in antenna payload type catalog.
 * <p>
 * Additional integrations can follow the same pattern and provide their own
 * {@link AntennaPayloadType} instances without changing the channel table.
 */
public final class AntennaPayloadTypes {
    /** Redstone aggregation uses the strongest in-range signal. */
    public static final AntennaPayloadType<RedstonePayload> REDSTONE = new AntennaPayloadType<>(
            RedstonePayload.ID,
            RedstonePayload.class,
            payloads -> payloads.stream().map(RedstonePayload::power).max(Comparator.naturalOrder()).map(RedstonePayload::new));

    /** Music aggregation uses the first in-range song source found. */
    public static final AntennaPayloadType<MusicDiscPayload> MUSIC_DISC = new AntennaPayloadType<>(
            MusicDiscPayload.ID,
            MusicDiscPayload.class,
            payloads -> payloads.stream().findFirst());

    /** Immutable list of payload types provided by this mod. */
    public static final List<AntennaPayloadType<? extends AntennaPayload>> BUILT_INS = List.of(REDSTONE, MUSIC_DISC);

    private AntennaPayloadTypes() {
    }

    /**
     * Looks up a built-in payload type by id.
     *
     * @param id payload type id
     * @return matching built-in type, if one exists
     */
    public static Optional<AntennaPayloadType<? extends AntennaPayload>> byId(net.minecraft.resources.Identifier id) {
        return BUILT_INS.stream().filter(type -> type.id().equals(id)).findFirst();
    }
}
