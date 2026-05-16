package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static final String ID = "create";
    public static boolean IS_LOADED = false;

    public static void init(IEventBus modEventBus) {
        if (!MainConfig.createCompatEnabled) {
            return;
        }

        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            if (MainConfig.createArmPotEnabled) {CreatePotArmCompat.init(modEventBus);}
            if (MainConfig.createArmStockpotEnabled) {CreateStockpotArmCompat.init(modEventBus);}
            if (MainConfig.createArmSteamerEnabled) {CreateSteamerArmCompat.init(modEventBus);}
            if (MainConfig.createArmMillstoneEnabled) {CreateMillstoneArmCompat.init(modEventBus);}
            if (MainConfig.createArmShawarmaSpitEnabled) {CreateShawarmaSpitArmCompat.init(modEventBus);}
            if (MainConfig.createArmTeapotEnabled) {CreateTeapotArmCompat.init(modEventBus);}
        });
    }
}