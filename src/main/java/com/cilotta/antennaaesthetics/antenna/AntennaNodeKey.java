package com.cilotta.antennaaesthetics.antenna;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record AntennaNodeKey(ResourceKey<Level> dimension, BlockPos pos) {
    public AntennaNodeKey(ResourceKey<Level> dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos.immutable();
    }
}
