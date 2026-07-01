package com.bmt.kaleidoscope_compat.compat.create.client;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, value = Dist.CLIENT)
public class CreateClientEventHandler {

    private static boolean isCreateLoaded() {
        return !ModList.get().isLoaded("create");
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        if (isCreateLoaded()) return;
        CreateClientCompat.registerGuiLayers(event);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (isCreateLoaded()) return;
        CreateClientCompat.onKeyInput(event);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (isCreateLoaded()) return;
        CreateClientCompat.onRenderLevelStage(event);
    }
}