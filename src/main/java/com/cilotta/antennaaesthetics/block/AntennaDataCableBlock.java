package com.cilotta.antennaaesthetics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Local antenna data cable.
 * <p>
 * Cable blocks do not carry wireless payloads themselves. Antenna bases scan a
 * connected cable graph to discover nearby converter blocks that can turn
 * vanilla or modded signals into antenna payloads.
 */
public class AntennaDataCableBlock extends Block {
    /**
     * Creates a cable block with registry-provided properties.
     *
     * @param properties configured block behavior
     */
    public AntennaDataCableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
