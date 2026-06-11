package com.bmt.kaleidoscope_compat.compat.farm_and_charm;

import com.github.ysbbbbbb.kaleidoscopecookery.api.event.StockpotMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;

import java.util.List;

@SuppressWarnings("unchecked")
public class CookingPotCompat {

    static void getTransformRecipeForJei(Level level, List<RecipeHolder<StockpotRecipe>> recipes) {
        if (level == null) {
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof CookingPotRecipe) {
                if (holder.id().getNamespace().equals("farm_and_charm") &&
                        holder.value().getType().toString().contains("pot_cooking")) {
                    recipes.add(transformRecipe((RecipeHolder<CookingPotRecipe>) holder, level));
                }
            }
        }
    }

    static RecipeHolder<StockpotRecipe> transformRecipe(RecipeHolder<CookingPotRecipe> holder, Level level) {
        CookingPotRecipe farmAndCharmRecipe = holder.value();
        StockpotRecipe recipe = new StockpotRecipe(
                farmAndCharmRecipe.getIngredients(),
                farmAndCharmRecipe.getResultItem(level.registryAccess()),
                200,
                farmAndCharmRecipe.getContainerItem()
        );
        return new RecipeHolder<>(holder.id(), recipe);
    }

    @SubscribeEvent
    static void afterStockpotRecipeMatch(StockpotMatchRecipeEvent.Post event) {
        ResourceLocation rawOutput = event.getRawOutput();
        RecipeManager recipeManager = event.getLevel().getRecipeManager();

        if (rawOutput != StockpotRecipeSerializer.EMPTY_ID) {
            return;
        }

        List<ItemStack> items = event.getInput().getInputs();
        RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(NonNullList.copyOf(items)));

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof CookingPotRecipe farmAndCharmRecipe) {
                if (holder.id().getNamespace().equals("farm_and_charm") &&
                        holder.value().getType().toString().contains("pot_cooking")) {

                    if (farmAndCharmRecipe.matches(wrapper, event.getLevel())) {
                        event.setOutput(transformRecipe((RecipeHolder<CookingPotRecipe>) holder, event.getLevel()));
                        return;
                    }
                }
            }
        }
    }
}