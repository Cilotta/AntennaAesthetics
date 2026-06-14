package com.cilotta.antennaaesthetics.data;

import java.util.stream.Stream;

import com.cilotta.antennaaesthetics.AntennaAesthetics;
import com.cilotta.antennaaesthetics.block.AntennaDataCableBlock;
import com.cilotta.antennaaesthetics.registry.ModBlocks;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Generates multipart client models for blocks whose appearance depends on
 * their state.
 */
public final class AntennaModelProvider extends ModelProvider {
    public AntennaModelProvider(PackOutput output) {
        super(output, AntennaAesthetics.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block cable = ModBlocks.ANTENNA_DATA_CABLE.get();
        var core = BlockModelGenerators.plainVariant(model("antenna_data_cable_core"));
        var arm = BlockModelGenerators.plainVariant(model("antenna_data_cable_arm"));

        blockModels.blockStateOutput.accept(MultiPartGenerator.multiPart(cable)
                .with(core)
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.NORTH, true), arm)
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.EAST, true),
                        arm.with(BlockModelGenerators.Y_ROT_90))
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.SOUTH, true),
                        arm.with(BlockModelGenerators.Y_ROT_180))
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.WEST, true),
                        arm.with(BlockModelGenerators.Y_ROT_270))
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.UP, true),
                        arm.with(BlockModelGenerators.X_ROT_90))
                .with(BlockModelGenerators.condition().term(AntennaDataCableBlock.DOWN, true),
                        arm.with(BlockModelGenerators.X_ROT_270)));

        blockModels.registerSimpleItemModel(cable, model("antenna_data_cable_core"));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(ModBlocks.ANTENNA_DATA_CABLE);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(ModBlocks.ANTENNA_DATA_CABLE_ITEM);
    }

    private static Identifier model(String path) {
        return Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, "block/" + path);
    }
}
