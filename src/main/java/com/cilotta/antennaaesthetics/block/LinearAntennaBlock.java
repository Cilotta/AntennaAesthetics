package com.cilotta.antennaaesthetics.block;

import com.cilotta.antennaaesthetics.blockentity.LinearAntennaBlockEntity;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class LinearAntennaBlock extends BaseEntityBlock {
    public LinearAntennaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(LinearAntennaBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new LinearAntennaBlockEntity(worldPosition, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LinearAntennaBlockEntity antenna)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            int channel = antenna.cycleChannel();
            tuneStructure(level, pos, channel);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.antennaaesthetics.linear_antenna.channel", channel));
            return InteractionResult.SUCCESS;
        }

        LinearAntennaScanResult result = assemble(level, pos);
        player.sendSystemMessage(result.message());
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LinearAntennaBlockEntity antenna)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (JukeboxSong.fromStack(itemStack).isPresent()) {
            if (!level.isClientSide()) {
                if (!antenna.isAssembled()) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.antennaaesthetics.linear_antenna.not_assembled"));
                    return InteractionResult.SUCCESS;
                }

                antenna.setSourceDisc(itemStack);
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.antennaaesthetics.linear_antenna.music_source", itemStack.getHoverName()));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            assemble(level, pos);
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LinearAntennaBlockEntity antenna) {
            antenna.removeFromChannel();
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof LinearAntennaBlockEntity antenna ? antenna.getReceivedRedstonePower() : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LINEAR_ANTENNA.get(), LinearAntennaBlockEntity::tick);
    }

    private static LinearAntennaScanResult assemble(Level level, BlockPos pos) {
        LinearAntennaScanResult result = LinearAntennaMultiblock.scan(level, pos);
        int channel = level.getBlockEntity(result.controllerPos()) instanceof LinearAntennaBlockEntity controller
                ? controller.getChannel()
                : 0;

        // Keep every element aware of the same controller, size, and frequency.
        for (BlockPos elementPos : result.elements()) {
            if (level.getBlockEntity(elementPos) instanceof LinearAntennaBlockEntity antenna) {
                antenna.setAssembly(result.controllerPos(), result.elementCount(), result.valid());
                antenna.setChannel(channel);
            }
        }
        return result;
    }

    private static void tuneStructure(Level level, BlockPos pos, int channel) {
        LinearAntennaScanResult result = LinearAntennaMultiblock.scan(level, pos);
        for (BlockPos elementPos : result.elements()) {
            if (level.getBlockEntity(elementPos) instanceof LinearAntennaBlockEntity antenna) {
                antenna.setChannel(channel);
            }
        }
    }
}
