package com.bmt.kaleidoscope_compat.mixin.kaleidoscope_tavern;

import com.bmt.kaleidoscope_compat.init.KCFluids;
import com.bmt.kaleidoscope_compat.init.KCItems;
import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KaleidoscopeTavern.class)
public class RegistryMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void kaleidoscopeCompat$registerItemsAndFluids(CallbackInfo ci) {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        KCFluids.FLUID_TYPES.register(modEventBus);
        KCFluids.FLUIDS.register(modEventBus);
        KCItems.ITEMS.register(modEventBus);
    }
}