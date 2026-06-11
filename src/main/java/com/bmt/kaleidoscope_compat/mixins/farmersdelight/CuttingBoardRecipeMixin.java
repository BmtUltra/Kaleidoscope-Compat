package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.category.FarmersDelightCategory;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;

@Mixin(CuttingBoardRecipe.class)
public class CuttingBoardRecipeMixin {

    @Inject(
            method = "matches*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCuttingBoardRecipes(CuttingBoardRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (FarmersDelightCategory.cuttingBoardRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}