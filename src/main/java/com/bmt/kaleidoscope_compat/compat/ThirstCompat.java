package com.bmt.kaleidoscope_compat.compat;

import com.bmt.kaleidoscope_compat.compat.thirst.ThirstConfigGenerator;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class ThirstCompat {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ThirstConfigGenerator::generateConfig);
    }
}