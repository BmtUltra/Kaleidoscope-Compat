package com.bmt.kaleidoscope_compat.mixin.farm_and_charm;

import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.StockpotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StockpotRecipeCategory.class)
public class StockpotRecipeCategoryMixin {

    @Inject(
            method = "getRecipes",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void kc$addFarmAndCharmRecipes(CallbackInfoReturnable<List<StockpotRecipe>> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        List<StockpotRecipe> recipes = Lists.newArrayList(cir.getReturnValue());
        FarmAndCharmCompat.getTransformRecipeForJei(level, recipes);
        cir.setReturnValue(recipes);
    }
}