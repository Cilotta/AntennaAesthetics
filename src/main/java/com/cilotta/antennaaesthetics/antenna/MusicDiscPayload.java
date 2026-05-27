package com.cilotta.antennaaesthetics.antenna;

import com.cilotta.antennaaesthetics.AntennaAesthetics;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;

/**
 * Transmitted music-disc signal. The stack is kept for display/future metadata,
 * while the resolved song holder is what the receiver plays.
 *
 * @param sourceStack disc item that produced the signal
 * @param song resolved jukebox song holder
 */
public record MusicDiscPayload(ItemStack sourceStack, Holder<JukeboxSong> song) implements AntennaPayload {
    /** Payload id used by the channel table. */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, "music_disc");

    /**
     * Stores one copy of the source disc so large item stacks are not retained.
     *
     * @param sourceStack disc item stack
     * @param song resolved song
     */
    public MusicDiscPayload(ItemStack sourceStack, Holder<JukeboxSong> song) {
        this.sourceStack = sourceStack.copyWithCount(1);
        this.song = song;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier typeId() {
        return ID;
    }
}
