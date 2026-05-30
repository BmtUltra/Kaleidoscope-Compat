package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static final String ID = "create";
    public static boolean IS_LOADED = false;

    public static void init(IEventBus modEventBus) {
        if (!MainConfig.createCompatEnabledValue) {
            return;
        }

        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            if (MainConfig.createArmPotEnabledValue) {CreatePotArmCompat.init(modEventBus);}
            if (MainConfig.createArmStockpotEnabledValue) {CreateStockpotArmCompat.init(modEventBus);}
            if (MainConfig.createArmSteamerEnabledValue) {CreateSteamerArmCompat.init(modEventBus);}
            if (MainConfig.createArmMillstoneEnabledValue) {CreateMillstoneArmCompat.init(modEventBus);}
            if (MainConfig.createArmShawarmaSpitEnabledValue) {CreateShawarmaSpitArmCompat.init(modEventBus);}
            if (MainConfig.createArmTeapotEnabledValue) {CreateTeapotArmCompat.init(modEventBus);}
        });
    }
}