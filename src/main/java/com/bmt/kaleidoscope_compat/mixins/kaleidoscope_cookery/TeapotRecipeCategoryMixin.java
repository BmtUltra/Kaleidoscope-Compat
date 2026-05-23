package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.TeapotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TeapotRecipeCategory.class)
public class TeapotRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<TeapotRecipe>>> cir) {
        List<RecipeHolder<TeapotRecipe>> originalRecipes = cir.getReturnValue();
        List<RecipeHolder<TeapotRecipe>> filteredRecipes = new ArrayList<>();

        for (RecipeHolder<TeapotRecipe> holder : originalRecipes) {
            TeapotRecipe recipe = holder.value();
            boolean shouldFilter = false;

            for (ItemStack input : recipe.ingredient().getItems()) {
                if (input.is(TagUtil.Items.TEAPOT_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && recipe.result().is(TagUtil.Items.TEAPOT_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                filteredRecipes.add(holder);
            }
        }
        cir.setReturnValue(filteredRecipes);
    }
}