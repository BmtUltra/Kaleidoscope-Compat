//package com.bmt.kaleidoscope_compat.client;
//
//import com.bmt.kaleidoscope_compat.client.renderer.StrawHatCurioRenderer;
//import com.github.ysbbbbbb.kaleidoscopecookery.client.model.StrawHatModel;
//import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
//import net.minecraft.client.Minecraft;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//import net.neoforged.neoforge.client.event.EntityRenderersEvent;
//import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
//
//@EventBusSubscriber(value = Dist.CLIENT)
//public class Client {
//
//    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//        event.enqueueWork(() -> {
//            if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
//                CuriosRendererRegistry.register(
//                        ModItems.STRAW_HAT.get(),
//                        () -> new StrawHatCurioRenderer(
//                                Minecraft.getInstance().getEntityModels().bakeLayer(StrawHatModel.LAYER_LOCATION)
//                        )
//                );
//
//                CuriosRendererRegistry.register(
//                        ModItems.STRAW_HAT_FLOWER.get(),
//                        () -> new StrawHatCurioRenderer(StrawHatModel.createBodyLayer().bakeRoot())
//                );
//            }
//        });
//    }
//
//    @SubscribeEvent
//    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
//        event.registerLayerDefinition(StrawHatModel.LAYER_LOCATION, StrawHatModel::createBodyLayer);
//    }
//}