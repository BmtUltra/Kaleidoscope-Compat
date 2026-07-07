package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.StockpotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(StockpotRecipeCategory.class)
public class StockpotRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<StockpotRecipe>>> cir) {
        List<RecipeHolder<StockpotRecipe>> originalRecipes = cir.getReturnValue();
        List<RecipeHolder<StockpotRecipe>> filteredRecipes = new ArrayList<>();

        for (RecipeHolder<StockpotRecipe> holder : originalRecipes) {
            StockpotRecipe recipe = holder.value();
            boolean shouldFilter = false;

            for (ItemStack input : recipe.getIngredients().stream()
                    .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                    .toList()) {
                if (input.is(TagUtil.Items.STOCKPOT_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && recipe.result().is(TagUtil.Items.STOCKPOT_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                filteredRecipes.add(holder);
            }
        }
        cir.setReturnValue(filteredRecipes);
    }

    @ModifyVariable(method = "draw*", at = @At("STORE"), name = "type")
    private Component modifyTypeText(Component original) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            return Component.empty();
        }
        return original;
    }

    @Inject(method = "getTitle", at = @At("RETURN"), cancellable = true)
    private void onGetTitle(CallbackInfoReturnable<Component> cir) {
        if (!KitchenCategory.fuzzyRecipesEnabled) {
            cir.setReturnValue(Component.translatable("block.kaleidoscope_cookery.stockpot"));
        }
    }
}