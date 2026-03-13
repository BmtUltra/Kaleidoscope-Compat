package com.bmt.kaleidoscope_compat.compat.curios;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CuriosCompat {
    private static final String MOD_ID = "curios";
    private static final boolean IS_LOADED;
    
    static {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }
    
    public static boolean isLoaded() {
        return IS_LOADED;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event) {
        if (IS_LOADED) {
            CuriosCompatInner.registerRenderers(event);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void registerLayerDefinitions() {
        if (IS_LOADED) {
            CuriosCompatInner.registerLayerDefinitions();
        }
    }
}