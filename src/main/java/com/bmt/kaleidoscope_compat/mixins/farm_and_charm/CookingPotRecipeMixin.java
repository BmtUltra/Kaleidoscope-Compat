package com.bmt.kaleidoscope_compat.mixins.farm_and_charm;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;

@Mixin(CookingPotRecipe.class)
public class CookingPotRecipeMixin {

    @Inject(
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptFarmAndCharmCookingPotRecipes(Container inventory, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.farmAndCharmCookingPotRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}