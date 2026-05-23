package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeapotRecipe.class)
public class TeapotRecipeMixin {
    @Shadow
    @Final
    private Ingredient ingredient;

    @Shadow
    @Final
    private ItemStack result;

    @Inject(method = "matches*", at = @At("HEAD"), cancellable = true)
    private void onMatches(TeapotInput container, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (container.getItemStack().is(TagUtil.Items.TEAPOT_INPUT_RECIPE)) {
            cir.setReturnValue(false);
            return;
        }

        if (this.result.is(TagUtil.Items.TEAPOT_OUTPUT_RECIPE)) {
            cir.setReturnValue(false);
        }
    }
}