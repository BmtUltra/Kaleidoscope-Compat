package com.bmt.kaleidoscope_compat.compat;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.thirst.ThirstConfigGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ThirstCompat {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ThirstConfigGenerator.generateConfig();
        });
    }
}