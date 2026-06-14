package com.cilotta.antennaaesthetics;

import org.slf4j.Logger;

import com.cilotta.antennaaesthetics.antenna.AntennaChannelTable;
import com.cilotta.antennaaesthetics.data.AntennaModelProvider;
import com.cilotta.antennaaesthetics.registry.ModBlockEntities;
import com.cilotta.antennaaesthetics.registry.ModBlocks;
import com.cilotta.antennaaesthetics.registry.ModMenus;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Main NeoForge mod entry point.
 * <p>
 * This class owns the deferred registers, wires common setup events, registers
 * the mod config, and exposes the mod id shared by every package.
 */
@Mod(AntennaAesthetics.MODID)
public class AntennaAesthetics {
    /** Namespace used for every registry entry in this mod. */
    public static final String MODID = "antennaaesthetics";
    /** Shared logger for common and client setup. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Deferred block register. */
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    /** Deferred item register. */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    /** Deferred block entity type register. */
    public static final DeferredRegister<BlockEntityType<? extends BlockEntity>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    /** Deferred menu type register. */
    public static final DeferredRegister<MenuType<? extends AbstractContainerMenu>> MENUS =
            DeferredRegister.create(Registries.MENU, MODID);
    /** Deferred creative tab register. */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /** Creative tab containing antenna blocks and local signal accessories. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANTENNA_TAB = CREATIVE_MODE_TABS.register("antenna_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.antennaaesthetics"))
            .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
            .icon(() -> ModBlocks.ANTENNA_BASE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.ANTENNA_BASE_ITEM.get());
                output.accept(ModBlocks.LINEAR_ANTENNA_ITEM.get());
                output.accept(ModBlocks.ANTENNA_DATA_CABLE_ITEM.get());
                output.accept(ModBlocks.REDSTONE_CONVERTER_ITEM.get());
            }).build());

    /**
     * Constructs the mod and attaches every deferred register to the mod event bus.
     *
     * @param modEventBus NeoForge mod event bus
     * @param modContainer mod container used to register config
     */
    public AntennaAesthetics(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register();
        ModBlockEntities.register();
        ModMenus.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherClientData);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Logs the currently loaded antenna config during common setup.
     *
     * @param event common setup event
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Antenna framework loaded: max={}, rangePerAntenna={}, channelsPerAntenna={}",
                Config.MAX_ANTENNA_COUNT.getAsInt(),
                Config.BLOCKS_PER_ANTENNA.getAsInt(),
                Config.CHANNELS_PER_ANTENNA.getAsInt());
    }

    private void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(AntennaModelProvider::new);
    }

    /**
     * Adds antenna blocks to the vanilla redstone creative tab.
     *
     * @param event creative tab build event
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.ANTENNA_BASE_ITEM);
            event.accept(ModBlocks.LINEAR_ANTENNA_ITEM);
            event.accept(ModBlocks.ANTENNA_DATA_CABLE_ITEM);
            event.accept(ModBlocks.REDSTONE_CONVERTER_ITEM);
        }
    }

    /**
     * Logs the channel table state as the server begins startup.
     *
     * @param event server starting event
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Antenna channel table ready with {} active channels", AntennaChannelTable.activeChannelCount());
    }
}
