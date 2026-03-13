package com.bmt.kaleidoscope_compat.compat.curios;

import com.bmt.kaleidoscope_compat.client.renderer.StrawHatCurioRenderer;
import com.github.ysbbbbbb.kaleidoscopecookery.client.model.StrawHatModel;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosCompatInner {
    
    @OnlyIn(Dist.CLIENT)
    public static void registerRenderers(FMLClientSetupEvent event) {
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
    
    @OnlyIn(Dist.CLIENT)
    public static void registerLayerDefinitions() {
    }
}