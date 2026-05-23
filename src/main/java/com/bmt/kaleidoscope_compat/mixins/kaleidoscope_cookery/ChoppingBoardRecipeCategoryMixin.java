package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.ChoppingBoardRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ChoppingBoardRecipeCategory.class)
public class ChoppingBoardRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<ChoppingBoardRecipe>>> cir) {
        List<RecipeHolder<ChoppingBoardRecipe>> originalRecipes = cir.getReturnValue();
        List<RecipeHolder<ChoppingBoardRecipe>> filteredRecipes = new ArrayList<>();

        for (RecipeHolder<ChoppingBoardRecipe> holder : originalRecipes) {
            ChoppingBoardRecipe recipe = holder.value();
            boolean shouldFilter = false;

            for (ItemStack input : recipe.getIngredient().getItems()) {
                if (input.is(TagUtil.Items.CHOPPING_BOARD_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && recipe.getResult().is(TagUtil.Items.CHOPPING_BOARD_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                filteredRecipes.add(holder);
            }
        }
        cir.setReturnValue(filteredRecipes);
    }
}