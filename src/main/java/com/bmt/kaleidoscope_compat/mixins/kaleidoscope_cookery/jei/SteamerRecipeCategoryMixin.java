package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.SteamerRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SteamerRecipeCategory.class)
public class SteamerRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<SteamerRecipe>>> cir) {
        List<RecipeHolder<SteamerRecipe>> originalRecipes = cir.getReturnValue();
        List<RecipeHolder<SteamerRecipe>> filteredRecipes = new ArrayList<>();

        for (RecipeHolder<SteamerRecipe> holder : originalRecipes) {
            SteamerRecipe recipe = holder.value();
            boolean shouldFilter = false;

            for (ItemStack input : recipe.getIngredient().getItems()) {
                if (input.is(TagUtil.Items.STEAMER_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && recipe.getResult().is(TagUtil.Items.STEAMER_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                filteredRecipes.add(holder);
            }
        }
        cir.setReturnValue(filteredRecipes);
    }
}