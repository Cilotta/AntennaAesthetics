package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.block.AntennaDataCableBlock;
import com.cilotta.antennaaesthetics.block.AntennaBaseBlock;
import com.cilotta.antennaaesthetics.block.LinearAntennaBlock;
import com.cilotta.antennaaesthetics.block.RedstoneConverterBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Block and block-item registrations for the antenna system.
 */
public final class ModBlocks {
    /** Core controller block for a multiblock antenna. */
    public static final DeferredBlock<AntennaBaseBlock> ANTENNA_BASE = AntennaAesthetics.BLOCKS.registerBlock(
            "antenna_base",
            AntennaBaseBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 8.0F));

    /** Item form of {@link #ANTENNA_BASE}. */
    public static final DeferredItem<BlockItem> ANTENNA_BASE_ITEM = AntennaAesthetics.ITEMS.registerSimpleBlockItem(
            "antenna_base",
            ANTENNA_BASE);

    /** Passive stackable segment block used above an antenna base. */
    public static final DeferredBlock<LinearAntennaBlock> LINEAR_ANTENNA = AntennaAesthetics.BLOCKS.registerBlock(
            "linear_antenna",
            LinearAntennaBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(1.5F, 6.0F)
                    .noOcclusion());

    /** Item form of {@link #LINEAR_ANTENNA}. */
    public static final DeferredItem<BlockItem> LINEAR_ANTENNA_ITEM = AntennaAesthetics.ITEMS.registerSimpleBlockItem(
            "linear_antenna",
            LINEAR_ANTENNA);

    /** Local cable block that links antenna bases to converter blocks. */
    public static final DeferredBlock<AntennaDataCableBlock> ANTENNA_DATA_CABLE = AntennaAesthetics.BLOCKS.registerBlock(
            "antenna_data_cable",
            AntennaDataCableBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.8F, 3.0F)
                    .noOcclusion());

    /** Item form of {@link #ANTENNA_DATA_CABLE}. */
    public static final DeferredItem<BlockItem> ANTENNA_DATA_CABLE_ITEM = AntennaAesthetics.ITEMS.registerSimpleBlockItem(
            "antenna_data_cable",
            ANTENNA_DATA_CABLE);

    /** Converter block that bridges vanilla redstone and antenna data cables. */
    public static final DeferredBlock<RedstoneConverterBlock> REDSTONE_CONVERTER = AntennaAesthetics.BLOCKS.registerBlock(
            "redstone_converter",
            RedstoneConverterBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.5F, 6.0F));

    /** Item form of {@link #REDSTONE_CONVERTER}. */
    public static final DeferredItem<BlockItem> REDSTONE_CONVERTER_ITEM = AntennaAesthetics.ITEMS.registerSimpleBlockItem(
            "redstone_converter",
            REDSTONE_CONVERTER);

    private ModBlocks() {
    }

    /**
     * Forces class loading so static deferred registrations are created before
     * the registers attach to the mod event bus.
     */
    public static void register() {
    }
}
