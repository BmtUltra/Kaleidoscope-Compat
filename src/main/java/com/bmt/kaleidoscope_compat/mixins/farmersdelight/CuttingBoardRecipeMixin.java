package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@Mixin(value = CuttingBoardRecipe.class,remap = false)
public class CuttingBoardRecipeMixin {

    @Inject(
            method = "matches(Lnet/minecraftforge/items/wrapper/RecipeWrapper;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptCuttingBoardRecipes(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeConfig.FARMERSDELIGHT_CUTTING_BOARD_RECIPES_DISABLED.get()) {
            cir.setReturnValue(false);
        }
    }
}