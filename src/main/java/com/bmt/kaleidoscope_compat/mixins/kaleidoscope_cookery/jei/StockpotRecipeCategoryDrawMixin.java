package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.StockpotRecipeCategory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StockpotRecipeCategory.class)
public class StockpotRecipeCategoryDrawMixin {

    @ModifyVariable(method = "draw*", at = @At("STORE"), name = "type")
    private Component modifyTypeText(Component original) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            return Component.empty();
        }
        return original;
    }
}