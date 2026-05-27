package com.cilotta.antennaaesthetics.block;

import java.util.ArrayList;
import java.util.List;

import com.cilotta.antennaaesthetics.Config;
import com.cilotta.antennaaesthetics.registry.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validator for the current linear antenna multiblock.
 * <p>
 * The structure is intentionally simple: one antenna base at the bottom and a
 * vertical stack of blocks tagged as linear antenna materials above it.
 * Configuration controls the maximum antenna count and the range produced by
 * each antenna block. There is no minimum length: short structures simply have
 * fewer supported channels and less range.
 */
public final class LinearAntennaMultiblock {
    private LinearAntennaMultiblock() {
    }

    /**
     * Scans the world upward from a base block and returns a validation result.
     *
     * @param level world reader used for block lookup
     * @param basePos position of the antenna base/controller
     * @return validation result with player-facing diagnostic text
     */
    public static LinearAntennaScanResult scanFromBase(LevelReader level, BlockPos basePos) {
        if (isLinearAntenna(level.getBlockState(basePos.below())) || isAntennaBase(level.getBlockState(basePos.below()))) {
            return invalid(basePos, List.of(), "message.antennaaesthetics.antenna_base.base_not_bottom");
        }

        List<BlockPos> antennas = new ArrayList<>();
        BlockPos cursor = basePos.above();
        while (isLinearAntenna(level.getBlockState(cursor))) {
            antennas.add(cursor.immutable());
            cursor = cursor.above();
            if (antennas.size() > maxAntennaCount()) {
                return invalid(basePos, antennas, "message.antennaaesthetics.antenna_base.too_many_antennas", maxAntennaCount());
            }
        }

        if (isAntennaBase(level.getBlockState(cursor))) {
            return invalid(basePos, antennas, "message.antennaaesthetics.antenna_base.extra_base");
        }

        return new LinearAntennaScanResult(
                true,
                basePos,
                List.copyOf(antennas),
                Component.translatable("message.antennaaesthetics.antenna_base.assembled", antennas.size(), antennas.size() * blocksPerAntenna()));
    }

    /**
     * Checks whether a block state is a linear antenna segment material.
     *
     * @param state state to inspect
     * @return true when the state is in {@link ModTags#LINEAR_ANTENNA_MATERIALS}
     */
    private static boolean isLinearAntenna(BlockState state) {
        return state.is(ModTags.LINEAR_ANTENNA_MATERIALS);
    }

    /**
     * Checks whether a block state is another antenna base.
     *
     * @param state state to inspect
     * @return true when the state belongs to {@link AntennaBaseBlock}
     */
    private static boolean isAntennaBase(BlockState state) {
        return state.getBlock() instanceof AntennaBaseBlock;
    }

    /**
     * Creates an invalid scan result with translated diagnostic text.
     *
     * @param basePos scanned base position
     * @param antennas antenna blocks collected before failure
     * @param key translation key for the failure message
     * @param args translation arguments
     * @return invalid scan result
     */
    private static LinearAntennaScanResult invalid(BlockPos basePos, List<BlockPos> antennas, String key, Object... args) {
        return new LinearAntennaScanResult(false, basePos, List.copyOf(antennas), Component.translatable(key, args));
    }

    /**
     * Reads the configured maximum antenna count.
     *
     * @return maximum number of linear antenna blocks allowed
     */
    public static int maxAntennaCount() {
        return Config.MAX_ANTENNA_COUNT.getAsInt();
    }

    /**
     * Reads how much transmission range each antenna block contributes.
     *
     * @return blocks of range per antenna segment
     */
    public static int blocksPerAntenna() {
        return Config.BLOCKS_PER_ANTENNA.getAsInt();
    }
}
