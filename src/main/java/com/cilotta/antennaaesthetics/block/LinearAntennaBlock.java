package com.cilotta.antennaaesthetics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Passive multiblock segment for the linear antenna.
 * <p>
 * This block intentionally has no block entity. All interaction, channel, audio,
 * and redstone behavior lives on {@link AntennaBaseBlock}; these segments only
 * contribute count and range when stacked above a base.
 */
public class LinearAntennaBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 0, 4, 12, 1, 12),
            Block.box(5, 1, 5, 11, 15, 11),
            Block.box(4, 15, 4, 12, 16, 12));

    /**
     * Creates a linear antenna segment with registry-provided block properties.
     *
     * @param properties configured block behavior
     */
    public LinearAntennaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
