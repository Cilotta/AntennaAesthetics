package com.cilotta.antennaaesthetics.antenna;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class AntennaPayloadTypes {
    public static final AntennaPayloadType<RedstonePayload> REDSTONE = new AntennaPayloadType<>(
            RedstonePayload.ID,
            RedstonePayload.class,
            payloads -> payloads.stream().map(RedstonePayload::power).max(Comparator.naturalOrder()).map(RedstonePayload::new));

    public static final AntennaPayloadType<MusicDiscPayload> MUSIC_DISC = new AntennaPayloadType<>(
            MusicDiscPayload.ID,
            MusicDiscPayload.class,
            payloads -> payloads.stream().findFirst());

    public static final List<AntennaPayloadType<? extends AntennaPayload>> BUILT_INS = List.of(REDSTONE, MUSIC_DISC);

    private AntennaPayloadTypes() {
    }

    public static Optional<AntennaPayloadType<? extends AntennaPayload>> byId(net.minecraft.resources.Identifier id) {
        return BUILT_INS.stream().filter(type -> type.id().equals(id)).findFirst();
    }
}
