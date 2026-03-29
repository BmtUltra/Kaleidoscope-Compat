package com.bmt.kaleidoscope_compat.mixin.farmersdelight;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@Mixin(CuttingBoardRecipe.class)
public class CuttingBoardRecipeMixin {

    @Inject(
            method = "matches*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCuttingBoardRecipes(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.cuttingBoardRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}