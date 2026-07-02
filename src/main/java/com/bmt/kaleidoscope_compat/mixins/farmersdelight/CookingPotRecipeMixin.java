package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@Mixin(value = CookingPotRecipe.class,remap = false)
public class CookingPotRecipeMixin {

    @Inject(
            method = "matches(Lnet/minecraftforge/items/wrapper/RecipeWrapper;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCookingPotRecipes(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeConfig.FARMERSDELIGHT_COOKING_POT_RECIPES_DISABLED.get()) {
            cir.setReturnValue(false);
        }
    }
}