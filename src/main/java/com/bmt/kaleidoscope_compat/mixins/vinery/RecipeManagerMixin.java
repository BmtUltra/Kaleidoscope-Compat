package com.bmt.kaleidoscope_compat.mixins.vinery;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.minecraft.world.item.crafting.Recipe;
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
    private <T extends Recipe<?>> void kc$interceptVineryBarrelRecipes(
            RecipeType<T> recipeType, CallbackInfoReturnable<List<T>> cir) {

        if (recipeType.toString().contains("wine_fermentation") && ForgeConfig.VINERY_BARREL_RECIPES_DISABLED.get()) {
            cir.setReturnValue(List.of());
        }
    }
}