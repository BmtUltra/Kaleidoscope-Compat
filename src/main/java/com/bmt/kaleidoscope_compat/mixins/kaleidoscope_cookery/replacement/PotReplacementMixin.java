package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeManager.class)
public class PotReplacementMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "pot";

    @Inject(
            method = "getAllRecipesFor",
            at = @At("RETURN"),
            cancellable = true)
    private <R extends net.minecraft.world.item.crafting.Recipe<?>> void onGetAllRecipesFor(
            net.minecraft.world.item.crafting.RecipeType<R> recipeType,
            CallbackInfoReturnable<List<RecipeHolder<R>>> cir) {
        if (recipeType == com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes.POT_RECIPE) {
            List<RecipeHolder<R>> recipes = cir.getReturnValue();
            List<RecipeHolder<R>> modifiedRecipes = new ArrayList<>();

            for (RecipeHolder<R> holder : recipes) {
                R recipe = holder.value();
                if (recipe instanceof PotRecipe potRecipe) {
                    PotRecipe modified = kaleidoscope_Compat_1_21_1_NeoForge$replacePotRecipeIngredients(potRecipe);
                    if (modified != potRecipe) {
                        modifiedRecipes.add(new RecipeHolder<>(holder.id(), (R) modified));
                    } else {
                        modifiedRecipes.add(holder);
                    }
                } else {
                    modifiedRecipes.add(holder);
                }
            }

            cir.setReturnValue(modifiedRecipes);
        }
    }

    @Unique
    private PotRecipe kaleidoscope_Compat_1_21_1_NeoForge$replacePotRecipeIngredients(PotRecipe recipe) {
        boolean modified = false;
        NonNullList<Ingredient> newIngredients = NonNullList.create();

        for (Ingredient ingredient : recipe.ingredients()) {
            Ingredient replaced = kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredient(ingredient);
            newIngredients.add(replaced);
            if (replaced != ingredient) {
                modified = true;
            }
        }

        if (modified) {
            return new PotRecipe(
                    recipe.time(),
                    recipe.stirFryCount(),
                    recipe.carrier(),
                    newIngredients,
                    recipe.result()
            );
        }
        return recipe;
    }

    @Unique
    private Ingredient kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) return ingredient;

        List<net.minecraft.world.item.ItemStack> newStacks = new ArrayList<>();
        boolean modified = false;

        for (net.minecraft.world.item.ItemStack stack : stacks) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            ResourceLocation tagReplacement = ReplacementManager.getTagReplacement(REPLACEMENT_TYPE, itemId);
            if (tagReplacement != null) {
                TagKey<Item> tagKey = ItemTags.create(tagReplacement);
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                    Item tagItem = holder.value();
                    if (tagItem != Items.AIR) {
                        newStacks.add(new net.minecraft.world.item.ItemStack(tagItem, stack.getCount()));
                    }
                }
                modified = true;
                continue;
            }

            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newStacks.add(new net.minecraft.world.item.ItemStack(newItem, stack.getCount()));
                    modified = true;
                }
            } else {
                newStacks.add(stack);
            }
        }

        if (modified) {
            return Ingredient.of(newStacks.stream());
        }
        return ingredient;
    }
}