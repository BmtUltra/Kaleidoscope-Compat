package com.bmt.kaleidoscope_compat.mixin.farmersdelight;

import com.bmt.kaleidoscope_compat.config.KCConfig;
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
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCookingPotRecipes(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        // 添加配置检查
        if (KCConfig.cookingPotRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}