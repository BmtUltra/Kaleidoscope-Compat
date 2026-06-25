package com.bmt.kaleidoscope_compat.mixins.farm_and_charm;

import com.bmt.kaleidoscope_compat.config.category.FarmAndCharmCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CookingPotRecipe.class)
public class CookingPotRecipeMixin {

    @Inject(
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptFarmAndCharmCookingPotRecipes(RecipeInput recipeInput, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (FarmAndCharmCategory.farmAndCharmCookingPotRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}