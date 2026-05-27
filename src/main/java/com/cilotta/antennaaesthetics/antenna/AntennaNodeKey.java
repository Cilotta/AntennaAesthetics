package com.cilotta.antennaaesthetics.antenna;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Unique world-space identity of an antenna node.
 *
 * @param dimension dimension containing the antenna
 * @param pos immutable position of the antenna base/controller
 */
public record AntennaNodeKey(ResourceKey<Level> dimension, BlockPos pos) {
    /**
     * Stores an immutable copy of the position so table keys cannot drift.
     *
     * @param dimension dimension containing the antenna
     * @param pos mutable or immutable block position
     */
    public AntennaNodeKey(ResourceKey<Level> dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos.immutable();
    }
}
