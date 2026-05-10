package com.bmt.kaleidoscope_compat.compat.thirst;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class ThirstCompat {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("thirst") || !MainConfig.thirstCompatEnabled) {
            return;
        }
        event.enqueueWork(ThirstConfigGenerator::generateConfig);
    }
}