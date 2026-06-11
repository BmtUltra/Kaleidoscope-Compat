package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.category.FarmersDelightCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@Mixin(CookingPotRecipe.class)
public class CookingPotRecipeMixin {

    @Inject(
            method = "matches*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCookingPotRecipes(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (FarmersDelightCategory.cookingPotRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}