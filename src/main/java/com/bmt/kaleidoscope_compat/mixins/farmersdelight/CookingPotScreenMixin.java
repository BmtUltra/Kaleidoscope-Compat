package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.category.FarmersDelightCategory;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;

@Mixin(CookingPotScreen.class)
public class CookingPotScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (FarmersDelightCategory.cookingPotGuiDisabled) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.closeContainer();
            }
            Minecraft.getInstance().setScreen(null);
            ci.cancel();
        }
    }
}