package com.cilotta.antennaaesthetics.antenna;

import com.cilotta.antennaaesthetics.AntennaAesthetics;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Transmitted redstone power level.
 *
 * @param power clamped redstone power, from 0 to 15
 */
public record RedstonePayload(int power) implements AntennaPayload {
    /** Payload id used by the channel table. */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, "redstone");

    /**
     * Creates a redstone payload and clamps the value to vanilla redstone bounds.
     *
     * @param power requested redstone power
     */
    public RedstonePayload(int power) {
        this.power = Mth.clamp(power, 0, 15);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier typeId() {
        return ID;
    }
}
