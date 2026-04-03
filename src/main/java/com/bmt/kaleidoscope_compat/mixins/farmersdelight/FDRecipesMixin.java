package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.integration.jei.FDRecipes;

import java.util.List;

@Mixin(FDRecipes.class)
public class FDRecipesMixin {

    @Inject(
            method = "getCookingPotRecipes",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void kc$interceptJEICookingPotRecipes(CallbackInfoReturnable<List<Recipe>> cir) {
        if (MainConfig.cookingPotRecipesDisabled) {
            cir.setReturnValue(List.of());
        }
    }

    @Inject(
            method = "getCuttingBoardRecipes",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void kc$interceptJEICuttingBoardRecipes(CallbackInfoReturnable<List<Recipe>> cir) {
        if (MainConfig.cuttingBoardRecipesDisabled) {
            cir.setReturnValue(List.of());
        }
    }
}