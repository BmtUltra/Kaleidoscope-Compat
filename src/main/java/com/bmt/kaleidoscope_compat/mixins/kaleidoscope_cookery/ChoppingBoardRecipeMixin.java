package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChoppingBoardRecipe.class)
public class ChoppingBoardRecipeMixin {
    @Inject(method = "matches*", at = @At("HEAD"), cancellable = true)
    private void onMatches(SingleRecipeInput inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (inv.getItem(0).is(TagUtil.Items.CHOPPING_BOARD_INPUT_RECIPE)) {
            cir.setReturnValue(false);
            return;
        }

        ChoppingBoardRecipe recipe = (ChoppingBoardRecipe) (Object) this;
        if (recipe.getResult().is(TagUtil.Items.CHOPPING_BOARD_OUTPUT_RECIPE)) {
            cir.setReturnValue(false);
        }
    }
}