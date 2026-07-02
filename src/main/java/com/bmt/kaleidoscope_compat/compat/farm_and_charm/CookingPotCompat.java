//package com.bmt.kaleidoscope_compat.compat.farm_and_charm;
//
//import com.github.ysbbbbbb.kaleidoscopecookery.api.event.StockpotMatchRecipeEvent;
//import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
//import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Recipe;
//import net.minecraft.world.item.crafting.RecipeManager;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.items.ItemStackHandler;
//import net.minecraftforge.items.wrapper.RecipeWrapper;
//import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
//
//import java.util.List;
//
//@SuppressWarnings("unchecked")
//public class CookingPotCompat {
//
//    static void getTransformRecipeForJei(Level level, List<StockpotRecipe> recipes) {
//        if (level == null) {
//            return;
//        }
//        RecipeManager recipeManager = level.getRecipeManager();
//
//        for (Recipe<?> recipe : recipeManager.getRecipes()) {
//            if (recipe instanceof CookingPotRecipe) {
//                if (recipe.getId().getNamespace().equals("farm_and_charm") &&
//                        recipe.getType().toString().contains("pot_cooking")) {
//                    recipes.add(transformRecipe((CookingPotRecipe) recipe, level));
//                }
//            }
//        }
//    }
//
//    static StockpotRecipe transformRecipe(CookingPotRecipe farmAndCharmRecipe, Level level) {
//        return new StockpotRecipe(
//                farmAndCharmRecipe.getId(),
//                farmAndCharmRecipe.getIngredients(),
//                farmAndCharmRecipe.getResultItem(level.registryAccess()),
//                200,
//                farmAndCharmRecipe.getContainerItem()
//        );
//    }
//
//    @SubscribeEvent
//    static void afterStockpotRecipeMatch(StockpotMatchRecipeEvent.Post event) {
//        StockpotRecipe rawOutput = event.getRawOutput();
//        RecipeManager recipeManager = event.getLevel().getRecipeManager();
//
//        if (rawOutput.getId() != StockpotRecipeSerializer.EMPTY_ID) {
//            return;
//        }
//
//        NonNullList<ItemStack> items = event.getContainer().getItems();
//        RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(items));
//
//        for (Recipe<?> recipe : recipeManager.getRecipes()) {
//            if (recipe instanceof CookingPotRecipe farmAndCharmRecipe) {
//                if (recipe.getId().getNamespace().equals("farm_and_charm") &&
//                        recipe.getType().toString().contains("pot_cooking")) {
//
//                    if (farmAndCharmRecipe.matches(wrapper, event.getLevel())) {
//                        event.setOutput(transformRecipe(farmAndCharmRecipe, event.getLevel()));
//                        return;
//                    }
//                }
//            }
//        }
//    }
//}