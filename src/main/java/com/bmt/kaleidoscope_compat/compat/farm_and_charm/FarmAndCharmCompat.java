package com.bmt.kaleidoscope_compat.compat.farm_and_charm;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class FarmAndCharmCompat {
    public static void init() {
        if (!MainConfig.farmAndCharmCompatEnabledValue) {
            return;
        }

        ModList.get().getModContainerById("farm_and_charm").ifPresent(modContainer -> {
            NeoForge.EVENT_BUS.addListener(CookingPotCompat::afterStockpotRecipeMatch);
        });
    }

    public static void getTransformRecipeForJei(Level level, List<RecipeHolder<StockpotRecipe>> recipes) {
        if (ModList.get().isLoaded("farm_and_charm")) {
            CookingPotCompat.getTransformRecipeForJei(level, recipes);
        }
    }
}