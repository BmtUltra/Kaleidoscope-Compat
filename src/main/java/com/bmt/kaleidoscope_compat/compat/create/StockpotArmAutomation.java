package com.bmt.kaleidoscope_compat.compat.create;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import org.jetbrains.annotations.Nullable;

public interface StockpotArmAutomation {
    @Nullable
    RecipeItem.RecipeRecord kaleidoscopeCompat$getStoredRecipe();

    void kaleidoscopeCompat$setStoredRecipe(@Nullable RecipeItem.RecipeRecord recipe);
}
