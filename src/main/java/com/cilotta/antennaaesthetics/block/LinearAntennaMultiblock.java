package com.cilotta.antennaaesthetics.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public final class LinearAntennaMultiblock {
    public static final int MIN_ELEMENTS = 3;
    public static final int MAX_ELEMENTS = 16;

    private LinearAntennaMultiblock() {
    }

    public static LinearAntennaScanResult scan(LevelReader level, BlockPos touchedPos) {
        BlockPos bottom = touchedPos;
        while (isLinearAntenna(level.getBlockState(bottom.below()))) {
            bottom = bottom.below();
        }

        List<BlockPos> elements = new ArrayList<>();
        BlockPos cursor = bottom;
        while (isLinearAntenna(level.getBlockState(cursor))) {
            elements.add(cursor.immutable());//immutable 不可更改的
            cursor = cursor.above();
            if (elements.size() > MAX_ELEMENTS) {
                return invalid(bottom, elements, "message.antennaaesthetics.linear_antenna.too_tall", MAX_ELEMENTS);
            }
        }

        if (elements.size() < MIN_ELEMENTS) {
            return invalid(bottom, elements, "message.antennaaesthetics.linear_antenna.too_short", MIN_ELEMENTS, elements.size());
        }

        if (isLinearAntenna(level.getBlockState(cursor))) {
            return invalid(bottom, elements, "message.antennaaesthetics.linear_antenna.too_tall", MAX_ELEMENTS);
        }

        return new LinearAntennaScanResult(
                true,
                bottom,
                List.copyOf(elements),
                Component.translatable("message.antennaaesthetics.linear_antenna.assembled", elements.size()));
    }

    private static boolean isLinearAntenna(BlockState state) {
        return state.getBlock() instanceof LinearAntennaBlock;
    }

    private static LinearAntennaScanResult invalid(BlockPos controllerPos, List<BlockPos> elements, String key, Object... args) {
        return new LinearAntennaScanResult(false, controllerPos, List.copyOf(elements), Component.translatable(key, args));
    }
}
