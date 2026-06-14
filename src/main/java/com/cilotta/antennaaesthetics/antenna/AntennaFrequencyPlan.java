package com.cilotta.antennaaesthetics.antenna;

import java.util.ArrayList;
import java.util.List;

import com.cilotta.antennaaesthetics.Config;

/**
 * Converts a physical antenna length into selectable in-game frequency channels.
 * <p>
 * Channels are stable frequency steps. Longer antennas add higher channels
 * while retaining every channel available to shorter antennas.
 */
public final class AntennaFrequencyPlan {
    /** Lowest channel id supported by the in-game transmitter. */
    public static final int MIN_CHANNEL = 0;
    /** Highest channel id supported by the in-game transmitter. */
    public static final int MAX_CHANNEL = 99;

    private AntennaFrequencyPlan() {
    }

    /**
     * Returns all selectable channels for an assembled antenna length.
     *
     * @param antennaCount number of antenna elements in the structure
     * @return ordered unique channel list
     */
    public static List<Integer> supportedChannels(int antennaCount) {
        if (antennaCount <= 0) {
            return List.of(MIN_CHANNEL);
        }

        int step = Config.CHANNELS_PER_ANTENNA.getAsInt();
        int optionCount = Math.min(antennaCount, Config.MAX_FREQUENCY_OPTIONS.getAsInt());
        List<Integer> channels = new ArrayList<>();
        for (int option = 1; option <= optionCount; option++) {
            int channel = Math.clamp(option * step, MIN_CHANNEL, MAX_CHANNEL);
            if (!channels.contains(channel)) {
                channels.add(channel);
            }
        }
        return channels.isEmpty() ? List.of(MIN_CHANNEL) : List.copyOf(channels);
    }

    /**
     * Snaps an arbitrary channel to the nearest channel supported by the length.
     *
     * @param current current stored channel
     * @param antennaCount number of antenna elements in the structure
     * @return supported channel closest to {@code current}
     */
    public static int nearestSupportedChannel(int current, int antennaCount) {
        return supportedChannels(antennaCount).stream()
                .min((left, right) -> Integer.compare(Math.abs(left - current), Math.abs(right - current)))
                .orElse(MIN_CHANNEL);
    }

    /**
     * Cycles through the supported channel list.
     *
     * @param current current stored channel
     * @param antennaCount number of antenna elements in the structure
     * @param delta signed step, usually +1 or -1
     * @return next supported channel
     */
    public static int cycleChannel(int current, int antennaCount, int delta) {
        List<Integer> channels = supportedChannels(antennaCount);
        int index = channels.indexOf(current);
        if (index < 0) {
            index = channels.indexOf(nearestSupportedChannel(current, antennaCount));
        }
        return channels.get(Math.floorMod(index + delta, channels.size()));
    }

    /**
     * Formats the supported channel list for compact GUI display.
     *
     * @param antennaCount number of antenna elements in the structure
     * @return comma-separated channel list
     */
    public static String describeChannels(int antennaCount) {
        return String.join(", ", supportedChannels(antennaCount).stream().map(String::valueOf).toList());
    }
}
