package com.cilotta.antennaaesthetics.registry;

import com.cilotta.antennaaesthetics.AntennaAesthetics;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Shared tag keys used by antenna structure and cable scanning logic.
 */
public final class ModTags {
    /** Blocks that count as vertical linear antenna elements. */
    public static final TagKey<Block> LINEAR_ANTENNA_MATERIALS = block("linear_antenna_materials");

    private ModTags() {
    }

    /**
     * Creates a block tag key under this mod's namespace.
     *
     * @param path tag path
     * @return block tag key
     */
    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, path));
    }
}
