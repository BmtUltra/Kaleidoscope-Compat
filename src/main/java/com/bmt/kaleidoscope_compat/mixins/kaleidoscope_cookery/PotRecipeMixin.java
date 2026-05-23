package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotRecipe.class)
public class PotRecipeMixin {
    @Shadow
    @Final
    private NonNullList<Ingredient> ingredients;

    @Shadow
    @Final
    private ItemStack result;

    @Inject(method = "matches*", at = @At("HEAD"), cancellable = true)
    private void onMatches(SimpleInput simpleInput, Level level, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack input : simpleInput.getInputs()) {
            if (input.is(TagUtil.Items.POT_INPUT_RECIPE)) {
                cir.setReturnValue(false);
                return;
            }
        }

        if (this.result.is(TagUtil.Items.POT_OUTPUT_RECIPE)) {
            cir.setReturnValue(false);
        }
    }
}