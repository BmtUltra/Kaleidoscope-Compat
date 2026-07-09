package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.client.MainClient;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.LittleMaidCompat;
import com.bmt.kaleidoscope_compat.compat.vinery.VineryBarrelCompatMain;
import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";

    public KaleidoscopeCompat() {
        ModContainer container = ModList.get().getModContainerById(MOD_ID)
                .orElseThrow(() -> new RuntimeException("Cannot find mod container for " + MOD_ID));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, ForgeConfig.SPEC, container));
        if (FMLEnvironment.dist.isClient()) {
            MainClient.init(container);
        }
        VineryBarrelCompatMain.init();
        LittleMaidCompat.init();
    }
}