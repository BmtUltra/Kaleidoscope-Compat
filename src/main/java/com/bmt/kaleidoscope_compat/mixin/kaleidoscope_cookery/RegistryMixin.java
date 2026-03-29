package com.bmt.kaleidoscope_compat.mixin.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.init.KCSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KaleidoscopeCookery.class)
public class RegistryMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void kaleidoscopeCompat$registerSoupBases(CallbackInfo ci) {
        KCSoupBases.registerAll();
    }
}