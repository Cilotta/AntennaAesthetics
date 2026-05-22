package com.cilotta.antennaaesthetics.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public record LinearAntennaScanResult(boolean valid, BlockPos controllerPos, List<BlockPos> elements, Component message) {
    public int elementCount() {
        return this.elements.size();
    }
}
