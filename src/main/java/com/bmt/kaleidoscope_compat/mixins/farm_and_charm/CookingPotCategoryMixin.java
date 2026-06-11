package com.bmt.kaleidoscope_compat.mixins.farm_and_charm;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.satisfy.farm_and_charm.core.compat.jei.category.CookingPotCategory;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CookingPotCategory.class)
public class CookingPotCategoryMixin {

    @Inject(
            method = "setRecipe*",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void kc$interceptJEICookingPotRecipes(IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        if (MainConfig.farmAndCharmCookingPotRecipesDisabled) {
            ci.cancel();
        }
    }
}