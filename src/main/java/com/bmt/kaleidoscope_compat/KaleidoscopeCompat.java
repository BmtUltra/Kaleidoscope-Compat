package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.compat.appleskin.AppleSkinCompat;
import com.bmt.kaleidoscope_compat.compat.create.CreateCompat;
import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.vinery.VineryBarrelCompatMain;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KaleidoscopeCompat.MOD_ID)
@SuppressWarnings("all")
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public KaleidoscopeCompat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MainConfig.SPEC);
        CreateCompat.init(modEventBus);
        FarmAndCharmCompat.init();
        VineryBarrelCompatMain.init();
        if (ModList.get().isLoaded("appleskin")) {
            NeoForge.EVENT_BUS.register(AppleSkinCompat.class);
        }
    }
}
