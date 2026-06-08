package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class SpectrumCompat {
    public static void init(IEventBus modEventBus) {
        if (!MainConfig.spectrumCompatEnabledValue) {
            return;
        }
        ModList.get().getModContainerById("spectrum").ifPresent(modContainer -> {
            if (MainConfig.spectrumPastelNodeCompatEnabledValue) {
                if (MainConfig.spectrumMillstoneItemHandlerEnabledValue) {SpectrumMillstoneItemHandler.init(modEventBus);}
                if (MainConfig.spectrumShawarmaSpitItemHandlerEnabledValue) {SpectrumShawarmaSpitItemHandler.init(modEventBus);}
                if (MainConfig.spectrumPotItemHandlerEnabledValue) {SpectrumPotItemHandler.init(modEventBus);}
                if (MainConfig.spectrumSteamerItemHandlerEnabledValue) {SpectrumSteamerItemHandler.init(modEventBus);}
                if (MainConfig.spectrumTrashCanItemHandlerEnabledValue) {SpectrumTrashCanItemHandler.init(modEventBus);}
                if (MainConfig.spectrumChoppingBoardItemHandlerEnabledValue) {SpectrumChoppingBoardItemHandler.init(modEventBus);}
                if (MainConfig.spectrumTeapotItemHandlerEnabledValue) {SpectrumTeapotItemHandler.init(modEventBus);}
            }
        });
    }
}