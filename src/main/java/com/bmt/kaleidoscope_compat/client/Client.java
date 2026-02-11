package com.bmt.kaleidoscope_compat.client;

import com.bmt.kaleidoscope_compat.client.renderer.StrawHatCurioRenderer;
import com.github.ysbbbbbb.kaleidoscopecookery.client.model.StrawHatModel;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Client {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(
                    ModItems.STRAW_HAT.get(),
                    () -> new StrawHatCurioRenderer(
                            Minecraft.getInstance().getEntityModels().bakeLayer(StrawHatModel.LAYER_LOCATION)
                    )
            );

            CuriosRendererRegistry.register(
                    ModItems.STRAW_HAT_FLOWER.get(),
                    () -> new StrawHatCurioRenderer(StrawHatModel.createBodyLayer().bakeRoot())
            );
        });

    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(StrawHatModel.LAYER_LOCATION, StrawHatModel::createBodyLayer);
    }
}