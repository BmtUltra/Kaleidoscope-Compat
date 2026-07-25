package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.bmt.kaleidoscope_compat.compat.create.PotArmAutomation;
import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public final class RecipeAutomation {
    private RecipeAutomation() {
    }

    public static ItemStack tryInsertReusableRecipe(BlockEntity blockEntity, ItemStack stack, boolean simulate) {
        RecipeItem.RecipeRecord recipe = RecipeItem.getRecipe(stack);
        if (recipe == null || !supports(blockEntity, recipe) || hasSameRecipe(blockEntity, recipe)) {
            return stack;
        }

        if (simulate) {
            ItemStack acceptedSignal = stack.copy();
            acceptedSignal.shrink(1);
            return acceptedSignal;
        }

        configure(blockEntity, recipe);
        return stack;
    }

    public static boolean isApplicableRecipe(BlockEntity blockEntity, ItemStack stack) {
        RecipeItem.RecipeRecord recipe = RecipeItem.getRecipe(stack);
        return recipe != null && supports(blockEntity, recipe);
    }

    public static boolean configure(BlockEntity blockEntity, ItemStack recipeStack) {
        RecipeItem.RecipeRecord recipe = RecipeItem.getRecipe(recipeStack);
        return recipe != null && configure(blockEntity, recipe);
    }

    public static boolean configure(BlockEntity blockEntity, RecipeItem.RecipeRecord recipe) {
        if (!supports(blockEntity, recipe)) {
            return false;
        }

        if (blockEntity instanceof PotArmAutomation automation) {
            automation.kaleidoscopeCompat$setStoredRecipe(recipe);
            return true;
        }
        if (blockEntity instanceof StockpotArmAutomation automation) {
            automation.kaleidoscopeCompat$setStoredRecipe(recipe);
            return true;
        }
        return false;
    }

    private static boolean supports(BlockEntity blockEntity, RecipeItem.RecipeRecord recipe) {
        return blockEntity instanceof PotBlockEntity
                && blockEntity instanceof PotArmAutomation
                && recipe.type().equals(RecipeItem.POT)
                || blockEntity instanceof StockpotBlockEntity
                && blockEntity instanceof StockpotArmAutomation
                && recipe.type().equals(RecipeItem.STOCKPOT);
    }

    private static boolean hasSameRecipe(BlockEntity blockEntity, RecipeItem.RecipeRecord recipe) {
        RecipeItem.RecipeRecord stored = null;
        if (blockEntity instanceof PotArmAutomation automation) {
            stored = automation.kaleidoscopeCompat$getStoredRecipe();
        } else if (blockEntity instanceof StockpotArmAutomation automation) {
            stored = automation.kaleidoscopeCompat$getStoredRecipe();
        }
        return recipesMatch(stored, recipe);
    }

    private static boolean recipesMatch(RecipeItem.RecipeRecord first, RecipeItem.RecipeRecord second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null
                || !first.type().equals(second.type())
                || first.flexRecipe() != second.flexRecipe()
                || !ItemStack.matches(first.output(), second.output())) {
            return false;
        }
        return stackListsMatch(first.input(), second.input());
    }

    private static boolean stackListsMatch(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i < first.size(); i++) {
            if (!ItemStack.matches(first.get(i), second.get(i))) {
                return false;
            }
        }
        return true;
    }
}
