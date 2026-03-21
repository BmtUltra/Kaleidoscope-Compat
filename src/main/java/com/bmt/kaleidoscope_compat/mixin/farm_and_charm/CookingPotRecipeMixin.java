package com.bmt.kaleidoscope_compat.mixin.farm_and_charm;

import com.bmt.kaleidoscope_compat.config.KCConfig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
import net.minecraft.world.item.crafting.RecipeInput;

@Mixin(CookingPotRecipe.class)
public class CookingPotRecipeMixin {

    @Inject(
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptFarmAndCharmCookingPotRecipes(RecipeInput recipeInput, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (KCConfig.farmAndCharmCookingPotRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}