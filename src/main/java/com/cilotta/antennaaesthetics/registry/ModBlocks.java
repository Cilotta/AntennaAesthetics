package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.block.LinearAntennaBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModBlocks {
    public static final DeferredBlock<LinearAntennaBlock> LINEAR_ANTENNA = AntennaAesthetics.BLOCKS.registerBlock(
            "linear_antenna",
            LinearAntennaBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(1.5F, 6.0F)
                    .noOcclusion());

    public static final DeferredItem<BlockItem> LINEAR_ANTENNA_ITEM = AntennaAesthetics.ITEMS.registerSimpleBlockItem(
            "linear_antenna",
            LINEAR_ANTENNA);

    private ModBlocks() {
    }

    public static void register() {
        // Forces static registration when the mod constructor loads registry classes.
    }
}
