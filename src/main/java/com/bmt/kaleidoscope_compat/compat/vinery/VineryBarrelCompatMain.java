package com.bmt.kaleidoscope_compat.compat.vinery;

import net.minecraftforge.fml.ModList;

public class VineryBarrelCompatMain {
    public static final String ID = "vinery";
    public static boolean IS_LOADED = false;

    public static void init() {
        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
        });
    }
}