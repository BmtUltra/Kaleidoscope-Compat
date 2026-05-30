package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.compat.create.CreateCompat;
import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.spectrum.SpectrumCompat;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.LittleMaidCompat;
import com.bmt.kaleidoscope_compat.compat.vinery.VineryBarrelCompatMain;
import com.bmt.kaleidoscope_compat.config.ClothConfig;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforgespi.Environment;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public KaleidoscopeCompat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MainConfig.SPEC);
        CreateCompat.init(modEventBus);
        SpectrumCompat.init(modEventBus);
        FarmAndCharmCompat.init();
        VineryBarrelCompatMain.init();
        LittleMaidCompat.init();
        if (ModList.get().isLoaded("cloth_config")) {
            if (Environment.get().getDist().isClient()) {
                ClothConfig.build(modContainer);
            }
        }
    }
}
