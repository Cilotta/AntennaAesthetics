package com.cilotta.antennaaesthetics.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Immutable result returned by a linear antenna structure scan.
 *
 * @param valid true when the structure satisfies all current config rules
 * @param basePos position of the unique antenna base that was scanned
 * @param antennas antenna segment positions found above the base
 * @param message translated message explaining success or failure
 */
public record LinearAntennaScanResult(boolean valid, BlockPos basePos, List<BlockPos> antennas, Component message) {
    /**
     * Returns the number of antenna segments found by the scan.
     *
     * @return antenna segment count
     */
    public int antennaCount() {
        return this.antennas.size();
    }

    /**
     * Creates a failure result for callers that cannot access a world.
     *
     * @param basePos expected base position
     * @return invalid result with a translated "missing level" message
     */
    public static LinearAntennaScanResult missingLevel(BlockPos basePos) {
        return new LinearAntennaScanResult(false, basePos, List.of(), Component.translatable("message.antennaaesthetics.antenna_base.missing_level"));
    }
}
