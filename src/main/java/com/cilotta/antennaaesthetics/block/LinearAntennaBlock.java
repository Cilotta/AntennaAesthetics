package com.cilotta.antennaaesthetics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Passive multiblock segment for the linear antenna.
 * <p>
 * This block intentionally has no block entity. All interaction, channel, audio,
 * and redstone behavior lives on {@link AntennaBaseBlock}; these segments only
 * contribute count and range when stacked above a base.
 */
public class LinearAntennaBlock extends Block {
    /**
     * Creates a linear antenna segment with registry-provided block properties.
     *
     * @param properties configured block behavior
     */
    public LinearAntennaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
