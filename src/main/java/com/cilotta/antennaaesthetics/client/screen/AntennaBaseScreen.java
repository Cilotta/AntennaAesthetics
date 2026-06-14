package com.cilotta.antennaaesthetics.client.screen;

import com.cilotta.antennaaesthetics.antenna.AntennaFrequencyPlan;
import com.cilotta.antennaaesthetics.menu.AntennaBaseMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the antenna base.
 * <p>
 * The screen displays read-only assembly and radio capability information.
 */
public class AntennaBaseScreen extends AbstractContainerScreen<AntennaBaseMenu> {
    private static final int TEXT_PRIMARY = 0xFFF4F4F4;
    private static final int TEXT_SECONDARY = 0xFFA7B0B5;

    /**
     * Creates the antenna base screen.
     *
     * @param menu synchronized antenna menu
     * @param playerInventory player inventory owning this screen
     * @param title screen title supplied by the block entity
     */
    public AntennaBaseScreen(AntennaBaseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 206, 144);
        this.inventoryLabelY = 10_000;
        this.titleLabelX = 12;
        this.titleLabelY = 10;
    }

    /**
     * Extracts the screen background into Minecraft 26.1's retained GUI render
     * state.
     *
     * @param graphics render-state extractor
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     * @param a partial tick alpha
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractTransparentBackground(graphics);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE0202020);
        graphics.fill(this.leftPos + 6, this.topPos + 6, this.leftPos + this.imageWidth - 6, this.topPos + this.imageHeight - 6, 0xFF2F3437);
        graphics.fill(this.leftPos + 10, this.topPos + 30, this.leftPos + this.imageWidth - 10, this.topPos + 92, 0xFF171A1C);
    }

    /**
     * Writes all dynamic status labels from synchronized menu data.
     *
     * @param graphics render-state extractor
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_PRIMARY, false);

        int y = 34;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.status"), this.menu.assembled()
                ? Component.translatable("screen.antennaaesthetics.antenna_base.status.ready")
                : Component.translatable("screen.antennaaesthetics.antenna_base.status.invalid"));
        y += 14;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.range"), Component.literal(this.menu.range() + " blocks"));
        y += 14;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.frequencies"), Component.literal(AntennaFrequencyPlan.describeChannels(this.menu.antennaCount())));
    }

    /**
     * Draws one label/value row in the status panel.
     *
     * @param graphics render-state extractor
     * @param y row y offset relative to the screen
     * @param label left label component
     * @param value right value component
     */
    private void drawLine(GuiGraphicsExtractor graphics, int y, Component label, Component value) {
        graphics.text(this.font, label, 16, y, TEXT_SECONDARY, false);
        graphics.text(this.font, value, 112, y, TEXT_PRIMARY, false);
    }

}
