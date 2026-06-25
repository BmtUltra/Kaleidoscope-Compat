package com.bmt.kaleidoscope_compat.compat.create;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import org.jetbrains.annotations.Nullable;

public interface PotArmAutomation {
    @Nullable
    RecipeItem.RecipeRecord kaleidoscopeCompat$getStoredRecipe();

    void kaleidoscopeCompat$setStoredRecipe(@Nullable RecipeItem.RecipeRecord recipe);

    void kaleidoscopeCompat$setStoredRecipeClient(@Nullable RecipeItem.RecipeRecord recipe);
}
