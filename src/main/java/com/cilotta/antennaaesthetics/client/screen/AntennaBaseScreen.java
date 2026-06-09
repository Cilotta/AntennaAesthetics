package com.cilotta.antennaaesthetics.client.screen;

import com.cilotta.antennaaesthetics.antenna.AntennaFrequencyPlan;
import com.cilotta.antennaaesthetics.menu.AntennaBaseMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the antenna base.
 * <p>
 * The screen displays synchronized menu data and sends channel up/down button
 * presses back through vanilla container button packets.
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
     * Adds channel adjustment buttons after the base container screen positions
     * itself.
     */
    @Override
    protected void init() {
        super.init();
        int y = this.topPos + 106;
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeChannel(AntennaBaseMenu.BUTTON_CHANNEL_DOWN))
                .bounds(this.leftPos + 118, y, 28, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeChannel(AntennaBaseMenu.BUTTON_CHANNEL_UP))
                .bounds(this.leftPos + 154, y, 28, 20)
                .build());
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
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.count"), Component.literal(this.menu.antennaCount() + " / " + this.menu.maxAntennaCount()));
        y += 14;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.power"), Component.literal(String.valueOf(this.menu.redstonePower())));
        y += 14;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.range"), Component.literal(this.menu.range() + " blocks"));
        y += 14;
        this.drawLine(graphics, y, Component.translatable("screen.antennaaesthetics.antenna_base.frequencies"), Component.literal(AntennaFrequencyPlan.describeChannels(this.menu.antennaCount())));

        graphics.text(this.font, Component.translatable("screen.antennaaesthetics.antenna_base.channel", this.menu.channel()), 12, 111, TEXT_PRIMARY, false);
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

    /**
     * Sends a channel change button event to the server.
     *
     * @param buttonId menu button id
     */
    private void changeChannel(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }
}
