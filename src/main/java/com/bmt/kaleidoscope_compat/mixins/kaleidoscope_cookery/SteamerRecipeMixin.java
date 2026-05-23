package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SteamerRecipe.class)
public class SteamerRecipeMixin {
    @Inject(method = "matches*", at = @At("HEAD"), cancellable = true)
    private void onMatches(SingleRecipeInput inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (inv.getItem(0).is(TagUtil.Items.STEAMER_INPUT_RECIPE)) {
            cir.setReturnValue(false);
            return;
        }

        SteamerRecipe recipe = (SteamerRecipe) (Object) this;
        if (recipe.getResult().is(TagUtil.Items.STEAMER_OUTPUT_RECIPE)) {
            cir.setReturnValue(false);
        }
    }
}