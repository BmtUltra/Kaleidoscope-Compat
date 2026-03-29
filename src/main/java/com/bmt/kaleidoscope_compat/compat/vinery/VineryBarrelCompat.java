//package com.bmt.kaleidoscope_compat.compat.vinery;
//
//import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
//import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.material.Fluids;
//import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
//
//import java.util.List;
//
//@SuppressWarnings("unchecked")
//public class VineryBarrelCompat {
//
//    public static void getTransformRecipeForJei(Level level, List<RecipeHolder<BarrelRecipe>> recipes) {
//        if (level == null) {
//            return;
//        }
//
//        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
//            if (holder.value() instanceof FermentationBarrelRecipe) {
//                if (holder.id().getNamespace().equals("vinery") &&
//                        holder.value().getType().toString().contains("wine_fermentation")) {
//                    recipes.add(transformRecipe((RecipeHolder<FermentationBarrelRecipe>) holder, level));
//                }
//            }
//        }
//    }
//
//    static RecipeHolder<BarrelRecipe> transformRecipe(RecipeHolder<FermentationBarrelRecipe> holder, Level level) {
//        FermentationBarrelRecipe vineryRecipe = holder.value();
//        NonNullList<Ingredient> ingredients = vineryRecipe.getIngredients();
//        Ingredient carrier = Ingredient.of(ModItems.EMPTY_BOTTLE);
//        ItemStack result = vineryRecipe.getResultItem(level.registryAccess());
//
//        int unitTime = 100;
//
//        BarrelRecipe barrelRecipe = new BarrelRecipe(
//                ingredients,
//                Fluids.WATER,
//                carrier,
//                result,
//                unitTime
//        );
//        return new RecipeHolder<>(holder.id(), barrelRecipe);
//    }
//}