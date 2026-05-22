package com.cilotta.antennaaesthetics.antenna;

import net.minecraft.resources.Identifier;

/**
 * Marker for data that can travel through an antenna channel.
 * Other integrations can add their own records/classes without changing the antenna table.
 */
public interface AntennaPayload {
    Identifier typeId();
}
