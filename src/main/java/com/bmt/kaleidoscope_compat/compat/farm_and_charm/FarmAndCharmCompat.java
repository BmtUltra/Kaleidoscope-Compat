package com.bmt.kaleidoscope_compat.compat.farm_and_charm;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class FarmAndCharmCompat {
    public static final String ID = "farm_and_charm";
    public static boolean IS_LOADED = false;

    public static void init() {
        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            NeoForge.EVENT_BUS.addListener(CookingPotCompat::afterStockpotRecipeMatch);
        });
    }

    public static void getTransformRecipeForJei(Level level, List<RecipeHolder<StockpotRecipe>> recipes) {
        if (IS_LOADED) {
            CookingPotCompat.getTransformRecipeForJei(level, recipes);
        }
    }
}