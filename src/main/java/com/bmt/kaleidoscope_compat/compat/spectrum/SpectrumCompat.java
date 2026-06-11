package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.bmt.kaleidoscope_compat.config.category.SpectrumCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class SpectrumCompat {
    public static void init(IEventBus modEventBus) {
        if (!SpectrumCategory.spectrumCompatEnabled) {
            return;
        }
        ModList.get().getModContainerById("spectrum").ifPresent(modContainer -> {
            if (SpectrumCategory.spectrumPastelNodeCompatEnabled) {
                if (SpectrumCategory.spectrumMillstoneItemHandlerEnabled) {SpectrumMillstoneItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumShawarmaSpitItemHandlerEnabled) {SpectrumShawarmaSpitItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumPotItemHandlerEnabled) {SpectrumPotItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumSteamerItemHandlerEnabled) {SpectrumSteamerItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumTrashCanItemHandlerEnabled) {SpectrumTrashCanItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumChoppingBoardItemHandlerEnabled) {SpectrumChoppingBoardItemHandler.init(modEventBus);}
                if (SpectrumCategory.spectrumTeapotItemHandlerEnabled) {SpectrumTeapotItemHandler.init(modEventBus);}
            }
        });
    }
}