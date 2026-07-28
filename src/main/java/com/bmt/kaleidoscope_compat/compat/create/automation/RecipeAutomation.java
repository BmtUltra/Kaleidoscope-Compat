package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.bmt.kaleidoscope_compat.compat.create.ArmRecipeAttachments;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

        blockEntity.setData(ArmRecipeAttachments.ARM_RECIPE, Optional.of(recipe));
        blockEntity.setChanged();
        blockEntity.syncData(ArmRecipeAttachments.ARM_RECIPE);
        return true;
    }

    private static boolean supports(BlockEntity blockEntity, RecipeItem.RecipeRecord recipe) {
        return blockEntity instanceof PotBlockEntity
                && recipe.type().equals(RecipeItem.POT)
                || blockEntity instanceof StockpotBlockEntity
                && recipe.type().equals(RecipeItem.STOCKPOT);
    }

    private static boolean hasSameRecipe(BlockEntity blockEntity, RecipeItem.RecipeRecord recipe) {
        return recipesMatch(getStoredRecipe(blockEntity), recipe);
    }

    public static @Nullable RecipeItem.RecipeRecord getStoredRecipe(BlockEntity blockEntity) {
        return blockEntity.getExistingData(ArmRecipeAttachments.ARM_RECIPE)
                .orElse(Optional.empty())
                .orElse(null);
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
