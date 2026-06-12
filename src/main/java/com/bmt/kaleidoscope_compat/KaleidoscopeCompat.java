package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.compat.create.CreateCompat;
import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.spectrum.SpectrumCompat;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.LittleMaidCompat;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public static Configurator CONFIGURATOR;

    public KaleidoscopeCompat(IEventBus modEventBus) {
        CONFIGURATOR = new Configurator(MOD_ID);
        CONFIGURATOR.register(MainConfig.class);

        CreateCompat.init(modEventBus);
        SpectrumCompat.init(modEventBus);
        FarmAndCharmCompat.init();
        LittleMaidCompat.init();
    }
}