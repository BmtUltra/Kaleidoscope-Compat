package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.registry.KCSoupBases;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat
{
    public static final String MOD_ID = "kaleidoscope_compat";

    public KaleidoscopeCompat(FMLJavaModLoadingContext context)
    {
        KCSoupBases.registerAll();
    }
}