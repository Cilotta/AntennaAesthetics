package com.cilotta.antennaaesthetics.block;

import com.cilotta.antennaaesthetics.blockentity.RedstoneConverterBlockEntity;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Converter that bridges local redstone wiring and antenna data cables.
 * <p>
 * Antenna bases read this converter as a local redstone input through connected
 * data cables, then write received remote redstone power back to it for vanilla
 * redstone output.
 */
public class RedstoneConverterBlock extends BaseEntityBlock {
    /**
     * Creates a redstone converter block.
     *
     * @param properties configured block behavior
     */
    public RedstoneConverterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Returns the block codec used by Minecraft's block serialization system.
     *
     * @return simple block codec
     */
    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(RedstoneConverterBlock::new);
    }

    /**
     * Creates the converter block entity.
     *
     * @param worldPosition converter position
     * @param blockState current block state
     * @return converter block entity
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new RedstoneConverterBlockEntity(worldPosition, blockState);
    }

    /**
     * Uses the normal model renderer.
     *
     * @param state block state being rendered
     * @return model render shape
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Marks the converter as a redstone source.
     *
     * @param state converter state
     * @return always true
     */
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /**
     * Emits received antenna power into adjacent vanilla redstone components.
     *
     * @param state converter state
     * @param level block lookup
     * @param pos converter position
     * @param direction query direction
     * @return received remote redstone power
     */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof RedstoneConverterBlockEntity converter ? converter.getReceivedPower() : 0;
    }

    /**
     * Emits direct power with the same strength as weak power.
     *
     * @param state converter state
     * @param level block lookup
     * @param pos converter position
     * @param direction query direction
     * @return direct power
     */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    /**
     * Provides a server ticker so converters can clear stale remote output if a
     * base is removed or disconnected.
     *
     * @param level current level
     * @param blockState converter state
     * @param type requested block entity type
     * @param <T> block entity implementation
     * @return matching ticker, or null for other types
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.REDSTONE_CONVERTER.get(), RedstoneConverterBlockEntity::tick);
    }
}
