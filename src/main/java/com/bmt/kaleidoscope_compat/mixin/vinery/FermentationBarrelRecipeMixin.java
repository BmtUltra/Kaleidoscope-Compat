package com.bmt.kaleidoscope_compat.mixin.vinery;

import com.bmt.kaleidoscope_compat.config.KCConfig;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.recipe.input.FermentationBarrelRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FermentationBarrelRecipe.class)
public class FermentationBarrelRecipeMixin {

    @Inject(
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptFermentationBarrelRecipes(FermentationBarrelRecipeInput input, Level world, CallbackInfoReturnable<Boolean> cir) {
        if (KCConfig.vineryBarrelRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}