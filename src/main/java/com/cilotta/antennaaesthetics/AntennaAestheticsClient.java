package com.cilotta.antennaaesthetics;

import net.minecraft.client.Minecraft;
import com.cilotta.antennaaesthetics.client.screen.AntennaBaseScreen;
import com.cilotta.antennaaesthetics.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only mod entry point.
 * <p>
 * This class registers screens and the NeoForge config UI extension. It is
 * annotated for {@link Dist#CLIENT}, so dedicated servers never load it.
 */
@Mod(value = AntennaAesthetics.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AntennaAesthetics.MODID, value = Dist.CLIENT)
public class AntennaAestheticsClient {
    /**
     * Registers the built-in NeoForge configuration screen factory.
     *
     * @param container client mod container
     */
    public AntennaAestheticsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * Runs lightweight client setup logging.
     *
     * @param event client setup event
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AntennaAesthetics.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    /**
     * Registers the antenna base screen for its menu type.
     *
     * @param event menu screen registration event
     */
    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ANTENNA_BASE.get(), AntennaBaseScreen::new);
    }
}
