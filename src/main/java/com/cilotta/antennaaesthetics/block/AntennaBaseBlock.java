package com.cilotta.antennaaesthetics.block;

import com.cilotta.antennaaesthetics.blockentity.AntennaBaseBlockEntity;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Controller block for an assembled antenna.
 * <p>
 * The base owns the block entity that scans the structure, stores tuning state,
 * opens the UI, publishes payloads, and emits received redstone output.
 */
public class AntennaBaseBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 14, 16),
            Block.box(2, 14, 2, 14, 15, 14),
            Block.box(4, 15, 4, 12, 16, 12));

    /**
     * Creates the antenna base block with registry-provided properties.
     *
     * @param properties configured block behavior
     */
    public AntennaBaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Returns the block codec used by Minecraft's block serialization system.
     *
     * @return simple block codec
     */
    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AntennaBaseBlock::new);
    }

    /**
     * Creates the controller block entity for this base.
     *
     * @param worldPosition base position
     * @param blockState current block state
     * @return new antenna base block entity
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new AntennaBaseBlockEntity(worldPosition, blockState);
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

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Opens the antenna base menu when the player right-clicks with an empty hand.
     *
     * @param state clicked block state
     * @param level current level
     * @param pos clicked position
     * @param player interacting player
     * @param hitResult hit result
     * @return interaction result consumed when a base entity exists
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AntennaBaseBlockEntity base)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            base.updateAssemblyFromWorld();
            serverPlayer.openMenu(base, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Registers a held music disc as this base's audio broadcast source.
     *
     * @param itemStack held stack
     * @param state clicked state
     * @param level current level
     * @param pos clicked position
     * @param player interacting player
     * @param hand interaction hand
     * @param hitResult hit result
     * @return interaction result
     */
    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AntennaBaseBlockEntity base)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (JukeboxSong.fromStack(itemStack).isPresent()) {
            if (!level.isClientSide()) {
                base.updateAssemblyFromWorld();
                if (!base.isAssembled()) {
                    player.sendSystemMessage(base.currentScan().message());
                    return InteractionResult.SUCCESS;
                }

                base.setSourceDisc(itemStack);
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.antennaaesthetics.antenna_base.music_source", itemStack.getHoverName()));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Refreshes the cached multiblock status when neighboring blocks change.
     *
     * @param state base state
     * @param level current level
     * @param pos base position
     * @param block changed block
     * @param orientation redstone update orientation
     * @param movedByPiston whether the neighbor change came from piston movement
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AntennaBaseBlockEntity base) {
            base.updateAssemblyFromWorld();
        }
    }

    /**
     * Removes this base from the channel table before normal block destruction.
     *
     * @param level level accessor
     * @param pos base position
     * @param state destroyed state
     */
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof AntennaBaseBlockEntity base) {
            base.removeFromChannel();
        }
        super.destroy(level, pos, state);
    }

    /**
     * Marks the base as a redstone source so received signals can be emitted.
     *
     * @param state base state
     * @return always true
     */
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /**
     * Emits the received remote redstone strength.
     *
     * @param state base state
     * @param level block lookup
     * @param pos base position
     * @param direction query direction
     * @return received redstone power, or zero without a block entity
     */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof AntennaBaseBlockEntity base ? base.getReceivedRedstonePower() : 0;
    }

    /**
     * Emits direct power with the same strength as weak power.
     *
     * @param state base state
     * @param level block lookup
     * @param pos base position
     * @param direction query direction
     * @return direct redstone power
     */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    /**
     * Provides the server tick function for antenna base block entities.
     *
     * @param level current level
     * @param blockState base state
     * @param type requested block entity type
     * @param <T> block entity implementation
     * @return matching ticker, or null for other types
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANTENNA_BASE.get(), AntennaBaseBlockEntity::tick);
    }
}
