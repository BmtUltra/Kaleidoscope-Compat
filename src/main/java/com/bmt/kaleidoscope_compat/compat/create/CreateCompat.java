package com.bmt.kaleidoscope_compat.compat.create;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static final String ID = "create";
    public static boolean IS_LOADED = false;

    public static void init(IEventBus modEventBus) {
        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            CreatePotArmCompat.init(modEventBus);
            CreateShawarmaSpitArmCompat.init(modEventBus);
            CreateSteamerArmCompat.init(modEventBus);
            CreateStockpotArmCompat.init(modEventBus);
            CreateTeapotArmCompat.init(modEventBus);
        });
    }
}
