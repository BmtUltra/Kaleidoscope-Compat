package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class SpectrumCompat {
    public static final String ID = "spectrum";
    public static boolean IS_LOADED = false;

    public static void init(IEventBus modEventBus) {
        if (!MainConfig.spectrumCompatEnabled) {
            return;
        }

        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            if (MainConfig.spectrumPastelNodeCompatEnabled) {
                if (MainConfig.spectrumMillstoneItemHandlerEnabled) {
                    SpectrumMillstoneItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumShawarmaSpitItemHandlerEnabled) {
                    SpectrumShawarmaSpitItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumPotItemHandlerEnabled) {
                    SpectrumPotItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumSteamerItemHandlerEnabled) {
                    SpectrumSteamerItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumTrashCanItemHandlerEnabled) {
                    SpectrumTrashCanItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumChoppingBoardItemHandlerEnabled) {
                    SpectrumChoppingBoardItemHandler.init(modEventBus);
                }
                if (MainConfig.spectrumTeapotItemHandlerEnabled) {
                    SpectrumTeapotItemHandler.init(modEventBus);
                }
            }
        });
    }
}