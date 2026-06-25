package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.MillstoneRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MillstoneRecipeCategory.class)
public class MillstoneRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<MillstoneRecipe>>> cir) {
        List<RecipeHolder<MillstoneRecipe>> originalRecipes = cir.getReturnValue();
        List<RecipeHolder<MillstoneRecipe>> filteredRecipes = new ArrayList<>();

        for (RecipeHolder<MillstoneRecipe> holder : originalRecipes) {
            MillstoneRecipe recipe = holder.value();
            boolean shouldFilter = false;

            for (ItemStack input : recipe.ingredient().getItems()) {
                if (input.is(TagUtil.Items.MILLSTONE_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter) {
                for (var output : recipe.results()) {
                    if (output.stack().is(TagUtil.Items.MILLSTONE_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                        break;
                    }
                }
            }

            if (!shouldFilter) {
                filteredRecipes.add(holder);
            }
        }
        cir.setReturnValue(filteredRecipes);
    }
}