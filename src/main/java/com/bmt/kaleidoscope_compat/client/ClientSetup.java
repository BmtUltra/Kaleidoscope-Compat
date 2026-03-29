package com.bmt.kaleidoscope_compat.client;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.client.model.StrawHatModel;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (isCuriosLoaded()) {
                registerCurioRenderers();
            }
        });
    }

    private static boolean isCuriosLoaded() {
        try {
            Class.forName("top.theillusivec4.curios.api.CuriosApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void registerCurioRenderers() {
        try {
            Class<?> curiosRendererRegistryClass = Class.forName("top.theillusivec4.curios.api.client.CuriosRendererRegistry");
            Class<?> strawHatCurioRendererClass = Class.forName("com.bmt.kaleidoscope_compat.client.renderer.StrawHatCurioRenderer");
            Method registerMethod = curiosRendererRegistryClass.getMethod("register",
                    net.minecraft.world.item.Item.class, java.util.function.Supplier.class);

            java.util.function.Supplier<?> strawHatRenderer = () -> {
                try {
                    return strawHatCurioRendererClass.getConstructor(net.minecraft.client.model.geom.ModelPart.class)
                            .newInstance(Minecraft.getInstance().getEntityModels().bakeLayer(StrawHatModel.LAYER_LOCATION));
                } catch (Exception e) {
                    return null;
                }
            };
            registerMethod.invoke(null, ModItems.STRAW_HAT.get(), strawHatRenderer);
            registerMethod.invoke(null, ModItems.STRAW_HAT_FLOWER.get(), strawHatRenderer);
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(StrawHatModel.LAYER_LOCATION, StrawHatModel::createBodyLayer);
    }
}