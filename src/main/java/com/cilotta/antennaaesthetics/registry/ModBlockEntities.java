package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.blockentity.LinearAntennaBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LinearAntennaBlockEntity>> LINEAR_ANTENNA =
            AntennaAesthetics.BLOCK_ENTITY_TYPES.register(
                    "linear_antenna",
                    () -> new BlockEntityType<>(LinearAntennaBlockEntity::new, ModBlocks.LINEAR_ANTENNA.get()));

    private ModBlockEntities() {
    }

    public static void register() {
        // Forces static registration when the mod constructor loads registry classes.
    }
}
