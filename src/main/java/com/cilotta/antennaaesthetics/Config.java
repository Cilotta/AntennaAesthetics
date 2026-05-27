package com.cilotta.antennaaesthetics;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common configuration values for antenna assembly and range calculation.
 * <p>
 * NeoForge exposes these values through the generated in-game config screen.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Maximum number of linear antenna blocks accepted above a base. */
    public static final ModConfigSpec.IntValue MAX_ANTENNA_COUNT = BUILDER
            .comment("Maximum number of linear antenna blocks above one antenna base.")
            .defineInRange("maxAntennaCount", 16, 1, 256);

    /** Range contribution, in blocks, from each linear antenna segment. */
    public static final ModConfigSpec.IntValue BLOCKS_PER_ANTENNA = BUILDER
            .comment("Transmission range gained from each linear antenna block.")
            .defineInRange("blocksPerAntenna", 16, 1, 1024);

    /** Multiplier used to turn antenna length into the highest supported channel. */
    public static final ModConfigSpec.IntValue CHANNELS_PER_ANTENNA = BUILDER
            .comment("Frequency model: highest resonant channel is antenna count multiplied by this value.")
            .defineInRange("channelsPerAntenna", 8, 1, 64);

    /** Maximum harmonics shown as selectable channels in the antenna UI. */
    public static final ModConfigSpec.IntValue MAX_FREQUENCY_OPTIONS = BUILDER
            .comment("Maximum number of selectable harmonic channels generated from antenna length.")
            .defineInRange("maxFrequencyOptions", 8, 1, 32);

    /** Maximum local data-cable distance scanned from one antenna base. */
    public static final ModConfigSpec.IntValue DATA_CABLE_SCAN_RANGE = BUILDER
            .comment("Maximum cable graph distance from an antenna base to connected converters.")
            .defineInRange("dataCableScanRange", 16, 1, 128);

    /** Common config specification registered by the mod container. */
    static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Utility class; instances are not needed.
     */
    private Config() {
    }
}
