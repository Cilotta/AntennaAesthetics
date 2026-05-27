package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.blockentity.AntennaBaseBlockEntity;
import com.cilotta.antennaaesthetics.blockentity.RedstoneConverterBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Block entity type registrations.
 */
public final class ModBlockEntities {
    /** Block entity type used by the antenna base controller block. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntennaBaseBlockEntity>> ANTENNA_BASE =
            AntennaAesthetics.BLOCK_ENTITY_TYPES.register(
                    "antenna_base",
                    () -> new BlockEntityType<>(AntennaBaseBlockEntity::new, ModBlocks.ANTENNA_BASE.get()));

    /** Block entity type used by redstone converter blocks. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneConverterBlockEntity>> REDSTONE_CONVERTER =
            AntennaAesthetics.BLOCK_ENTITY_TYPES.register(
                    "redstone_converter",
                    () -> new BlockEntityType<>(RedstoneConverterBlockEntity::new, ModBlocks.REDSTONE_CONVERTER.get()));

    private ModBlockEntities() {
    }

    /**
     * Forces static registration before deferred registers are attached.
     */
    public static void register() {
    }
}
