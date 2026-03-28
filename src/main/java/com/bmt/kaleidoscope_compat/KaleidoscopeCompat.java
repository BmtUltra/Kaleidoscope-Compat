package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.vinery.VineryBarrelCompatMain;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.bmt.kaleidoscope_compat.init.KCSoupBases;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public KaleidoscopeCompat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MainConfig.SPEC);
        KCSoupBases.registerAll();
        FarmAndCharmCompat.init();
        VineryBarrelCompatMain.init();
    }
}
