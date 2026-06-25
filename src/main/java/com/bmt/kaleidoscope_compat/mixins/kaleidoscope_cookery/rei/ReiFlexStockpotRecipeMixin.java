package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.rei;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiFlexStockpotRecipeCategory;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReiFlexStockpotRecipeCategory.class)
public class ReiFlexStockpotRecipeMixin {

    @Inject(method = "registerCategories", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterCategories(CategoryRegistry registry, CallbackInfo ci) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            ci.cancel();
        }
    }
}