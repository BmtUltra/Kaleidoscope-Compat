package com.bmt.kaleidoscope_compat.compat.farm_and_charm;

import com.github.ysbbbbbb.kaleidoscopecookery.api.event.StockpotMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotContainer;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotVisuals;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;

import java.util.List;

public class CookingPotCompat {

    static void getTransformRecipeForJei(Level level, List<StockpotRecipe> recipes) {
        if (level == null) {
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe instanceof CookingPotRecipe cookingPotRecipe) {
                if (recipe.getId().getNamespace().equals("farm_and_charm") &&
                        recipe.getType().toString().contains("pot_cooking")) {
                    recipes.add(transformRecipe(cookingPotRecipe, level));
                }
            }
        }
    }

    static StockpotRecipe transformRecipe(CookingPotRecipe farmAndCharmRecipe, Level level) {
        ItemStack containerItem = farmAndCharmRecipe.getContainerItem();
        Ingredient carrier = containerItem.isEmpty() ? Ingredient.EMPTY : Ingredient.of(containerItem);

        return new StockpotRecipe(
                farmAndCharmRecipe.getId(),
                farmAndCharmRecipe.getIngredients(),
                StockpotRecipeSerializer.DEFAULT_SOUP_BASE,
                farmAndCharmRecipe.getResultItem(level.registryAccess()),
                200,
                carrier,
                StockpotVisuals.DEFAULT
        );
    }

    @SubscribeEvent
    static void afterStockpotRecipeMatch(StockpotMatchRecipeEvent.Post event) {
        ResourceLocation rawOutputId = event.getRawOutput();
        RecipeManager recipeManager = event.getLevel().getRecipeManager();

        if (!rawOutputId.equals(StockpotRecipeSerializer.EMPTY_ID)) {
            return;
        }

        StockpotContainer container = event.getContainer();
        Level level = event.getLevel();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe instanceof CookingPotRecipe farmAndCharmRecipe) {
                if (recipe.getId().getNamespace().equals("farm_and_charm") &&
                        recipe.getType().toString().contains("pot_cooking")) {

                    if (farmAndCharmRecipe.matches(container, level)) {
                        event.setOutput(transformRecipe(farmAndCharmRecipe, level));
                        return;
                    }
                }
            }
        }
    }
}