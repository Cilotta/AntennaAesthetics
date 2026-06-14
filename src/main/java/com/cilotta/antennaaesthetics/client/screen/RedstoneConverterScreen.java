package com.cilotta.antennaaesthetics.client.screen;

import com.cilotta.antennaaesthetics.menu.RedstoneConverterMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Configures the independent transmit and receive channels of a converter. */
public class RedstoneConverterScreen extends AbstractContainerScreen<RedstoneConverterMenu> {
    private static final int TEXT_PRIMARY = 0xFFF4F4F4;
    private static final int TEXT_SECONDARY = 0xFFA7B0B5;

    public RedstoneConverterScreen(RedstoneConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 206, 144);
        this.inventoryLabelY = 10_000;
        this.titleLabelX = 12;
        this.titleLabelY = 10;
    }

    @Override
    protected void init() {
        super.init();
        this.addChannelButtons(this.topPos + 52, RedstoneConverterMenu.BUTTON_INPUT_DOWN,
                RedstoneConverterMenu.BUTTON_INPUT_UP);
        this.addChannelButtons(this.topPos + 84, RedstoneConverterMenu.BUTTON_OUTPUT_DOWN,
                RedstoneConverterMenu.BUTTON_OUTPUT_UP);
    }

    private void addChannelButtons(int y, int downButton, int upButton) {
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeChannel(downButton))
                .bounds(this.leftPos + 118, y, 28, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeChannel(upButton))
                .bounds(this.leftPos + 154, y, 28, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractTransparentBackground(graphics);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE0202020);
        graphics.fill(this.leftPos + 6, this.topPos + 6, this.leftPos + this.imageWidth - 6,
                this.topPos + this.imageHeight - 6, 0xFF2F3437);
        graphics.fill(this.leftPos + 10, this.topPos + 30, this.leftPos + this.imageWidth - 10,
                this.topPos + 116, 0xFF171A1C);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_PRIMARY, false);
        this.drawLine(graphics, 38, Component.translatable("screen.antennaaesthetics.redstone_converter.input"),
                channelLabel(this.menu.inputChannel()));
        this.drawLine(graphics, 70, Component.translatable("screen.antennaaesthetics.redstone_converter.output"),
                channelLabel(this.menu.outputChannel()));
        this.drawLine(graphics, 102, Component.translatable("screen.antennaaesthetics.redstone_converter.power"),
                Component.literal(String.valueOf(this.menu.receivedPower())));
    }

    private void drawLine(GuiGraphicsExtractor graphics, int y, Component label, Component value) {
        graphics.text(this.font, label, 16, y, TEXT_SECONDARY, false);
        graphics.text(this.font, value, 112, y, TEXT_PRIMARY, false);
    }

    private static Component channelLabel(int channel) {
        return channel < 0 ? Component.translatable("screen.antennaaesthetics.redstone_converter.off")
                : Component.literal(String.valueOf(channel));
    }

    private void changeChannel(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }
}
