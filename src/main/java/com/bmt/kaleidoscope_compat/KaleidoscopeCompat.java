package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.capability.PotItemHandler;
import com.bmt.kaleidoscope_compat.command.BarrelCommand;
import com.bmt.kaleidoscope_compat.compat.create.CreateCompat;
import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.kaleidoscope_doll.KaleidoscopeDollCompat;
import com.bmt.kaleidoscope_compat.compat.spectrum.SpectrumCompat;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.LittleMaidCompat;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import com.bmt.kaleidoscope_compat.datamap.soup.StockpotVisualOverrideManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public static Configurator CONFIGURATOR;

    private static final StockpotVisualOverrideManager STOCKPOT_VISUAL_MANAGER = new StockpotVisualOverrideManager();
    private static final ReplacementManager TOMATO_REPLACEMENT_MANAGER = new ReplacementManager();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public KaleidoscopeCompat(IEventBus modEventBus) {
        CONFIGURATOR = new Configurator(MOD_ID);
        CONFIGURATOR.register(MainConfig.class);

        CreateCompat.init(modEventBus);
        SpectrumCompat.init(modEventBus);
        FarmAndCharmCompat.init();
        LittleMaidCompat.init();
        KaleidoscopeDollCompat.init(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
        if (ModList.get().isLoaded("kaleidoscope_tavern")) {
            NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.POT_BE.get(),
                (pot, side) -> new PotItemHandler(pot)
        );
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        BarrelCommand.register(event.getDispatcher());
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(STOCKPOT_VISUAL_MANAGER);
        event.addListener(TOMATO_REPLACEMENT_MANAGER);
    }
}