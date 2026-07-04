package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.bmt.kaleidoscope_compat.config.category.SpectrumCategory;
import com.bmt.kaleidoscope_compat.config.category.spectrum.NetworkNodeConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public class SpectrumCompat {
    public static void init(IEventBus modEventBus) {
        if (!SpectrumCategory.spectrumCompatEnabled) {
            return;
        }
        ModList.get().getModContainerById("spectrum").ifPresent(modContainer -> {
            if (SpectrumCategory.spectrumPastelNodeCompatEnabled) {
                if (NetworkNodeConfig.millstoneItemHandlerEnabled) {SpectrumMillstoneItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.shawarmaSpitItemHandlerEnabled) {SpectrumShawarmaSpitItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.potItemHandlerEnabled) {SpectrumPotItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.steamerItemHandlerEnabled) {SpectrumSteamerItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.trashCanItemHandlerEnabled) {SpectrumTrashCanItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.choppingBoardItemHandlerEnabled) {SpectrumChoppingBoardItemHandler.init(modEventBus);}
                if (NetworkNodeConfig.teapotItemHandlerEnabled) {SpectrumTeapotItemHandler.init(modEventBus);}
            }
            if (SpectrumCategory.spectrumMillstoneAnvilCrushingEnabled) {
                NeoForge.EVENT_BUS.addListener(MillstoneAnvilCrushingCompat::afterMillstoneRecipeMatch);
            }
        });
    }
}