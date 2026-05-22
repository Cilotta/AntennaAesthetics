package com.cilotta.antennaaesthetics.antenna;

import com.cilotta.antennaaesthetics.AntennaAesthetics;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public record RedstonePayload(int power) implements AntennaPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, "redstone");

    public RedstonePayload(int power) {
        this.power = Mth.clamp(power, 0, 15);
    }

    @Override
    public Identifier typeId() {
        return ID;
    }
}
