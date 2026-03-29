package com.bmt.kaleidoscope_compat.compat.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("all")
public class RecipeHiding implements IRecipeManagerPlugin {
    
    @Override
    public <V> @NotNull List<RecipeType<?>> getRecipeTypes(@NotNull IFocus<V> focus) {
        return Collections.emptyList();
    }
    
    @Override
    public <T, V> @NotNull List<T> getRecipes(@NotNull IRecipeCategory<T> recipeCategory, @NotNull IFocus<V> focus) {
        return Collections.emptyList();
    }
    
    @Override
    public <T> @NotNull List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        recipeCategory.getRecipeType();
        return Collections.emptyList();
    }
}