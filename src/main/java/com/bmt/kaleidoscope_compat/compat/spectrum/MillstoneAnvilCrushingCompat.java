package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.api.event.MillstoneMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.output.RandomOutput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.MillstoneRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import de.dafuqs.spectrum.recipe.anvil_crushing.AnvilCrushingRecipe;
import de.dafuqs.spectrum.registries.SpectrumRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class MillstoneAnvilCrushingCompat {
    public static void getTransformRecipeForJei(Level level, List<RecipeHolder<MillstoneRecipe>> recipes) {
        if (level == null) {
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof AnvilCrushingRecipe anvilCrushingRecipe) {
                if (holder.id().getNamespace().equals("spectrum")) {
                    recipes.add(transformRecipe(holder, anvilCrushingRecipe, level));
                }
            }
        }
    }

    private static RecipeHolder<MillstoneRecipe> transformRecipe(RecipeHolder<?> holder, AnvilCrushingRecipe anvilCrushingRecipe, Level level) {
        Ingredient ingredient = anvilCrushingRecipe.getIngredients().getFirst();
        ItemStack result = anvilCrushingRecipe.getResultItem(level.registryAccess()).copy();

        RandomOutput randomOutput = new RandomOutput(result, 1.0f);
        NonNullList<RandomOutput> results = NonNullList.create();
        results.add(randomOutput);

        MillstoneRecipe millstoneRecipe = new MillstoneRecipe(ingredient, results);
        return new RecipeHolder<>(holder.id(), millstoneRecipe);
    }

    @SubscribeEvent
    static void afterMillstoneRecipeMatch(MillstoneMatchRecipeEvent.Post event) {
        RecipeHolder<MillstoneRecipe> rawOutput = event.getRawOutput();
        if (rawOutput.id() != MillstoneRecipeSerializer.EMPTY_ID) {
            return;
        }

        Level level = event.getLevel();
        List<ItemStack> items = event.getInput().getInputs();
        if (items.isEmpty()) {
            return;
        }

        ItemStack firstItem = items.getFirst();
        SimpleInput input = new SimpleInput(List.of(firstItem));
        if (level.getRecipeManager().getRecipeFor(ModRecipes.MILLSTONE_RECIPE, input, level).isPresent()) {
            return;
        }

        SingleRecipeInput spectrumInput = new SingleRecipeInput(firstItem);
        Optional<RecipeHolder<AnvilCrushingRecipe>> optionalRecipe = level.getRecipeManager()
                .getRecipeFor(SpectrumRecipeTypes.ANVIL_CRUSHING, spectrumInput, level);

        optionalRecipe.ifPresent(recipe -> {
            if (recipe.id().getNamespace().equals("spectrum")) {
                event.setOutput(transformRecipe(recipe, recipe.value(), level));
            }
        });
    }
}