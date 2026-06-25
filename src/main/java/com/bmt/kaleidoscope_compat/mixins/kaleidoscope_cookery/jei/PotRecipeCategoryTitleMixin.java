package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.PotRecipeCategory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotRecipeCategory.class)
public class PotRecipeCategoryTitleMixin {

    @Inject(method = "getTitle", at = @At("RETURN"), cancellable = true)
    private void onGetTitle(CallbackInfoReturnable<Component> cir) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            cir.setReturnValue(Component.translatable("block.kaleidoscope_cookery.pot"));
        }
    }
}