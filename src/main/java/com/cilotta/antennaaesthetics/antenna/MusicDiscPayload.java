package com.cilotta.antennaaesthetics.antenna;

import com.cilotta.antennaaesthetics.AntennaAesthetics;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;

public record MusicDiscPayload(ItemStack sourceStack, Holder<JukeboxSong> song) implements AntennaPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AntennaAesthetics.MODID, "music_disc");

    public MusicDiscPayload(ItemStack sourceStack, Holder<JukeboxSong> song) {
        this.sourceStack = sourceStack.copyWithCount(1);
        this.song = song;
    }

    @Override
    public Identifier typeId() {
        return ID;
    }
}
