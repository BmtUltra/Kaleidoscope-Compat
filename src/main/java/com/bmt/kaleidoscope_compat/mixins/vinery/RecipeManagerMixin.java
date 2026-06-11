package com.bmt.kaleidoscope_compat.mixins.vinery;

import com.bmt.kaleidoscope_compat.config.category.VineryCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
            method = "getAllRecipesFor",
            at = @At("RETURN"),
            cancellable = true
    )
    private <T extends net.minecraft.world.item.crafting.Recipe<?>> void kc$interceptVineryBarrelRecipes(
            RecipeType<T> recipeType, CallbackInfoReturnable<List<RecipeHolder<T>>> cir) {

        if (recipeType.toString().contains("wine_fermentation") && VineryCategory.vineryBarrelRecipesDisabled) {
            cir.setReturnValue(List.of());
        }
    }
}