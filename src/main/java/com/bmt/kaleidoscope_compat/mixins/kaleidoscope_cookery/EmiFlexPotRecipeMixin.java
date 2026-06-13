package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiFlexPotRecipe;
import dev.emi.emi.api.EmiRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiFlexPotRecipe.class)
public class EmiFlexPotRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            ci.cancel();
        }
    }
}