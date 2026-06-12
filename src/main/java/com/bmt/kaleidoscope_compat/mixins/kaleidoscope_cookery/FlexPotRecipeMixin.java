package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.FlexPotRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlexPotRecipe.class)
public class FlexPotRecipeMixin {

    @Inject(method = "matches*", at = @At("HEAD"), cancellable = true)
    private void onMatches(SimpleInput simpleInput, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            cir.setReturnValue(false);
        }
    }
}