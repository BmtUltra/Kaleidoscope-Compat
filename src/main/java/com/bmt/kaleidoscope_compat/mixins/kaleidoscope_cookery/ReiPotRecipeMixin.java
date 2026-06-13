package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.category.KitchenCategory;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiPotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(ReiPotRecipeCategory.class)
public class ReiPotRecipeMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.POT_RECIPE)
                .forEach(r -> {
                    PotRecipe recipe = r.value();

                    boolean shouldFilter = false;
                    for (ItemStack input : recipe.getIngredients().stream()
                            .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                            .toList()) {
                        if (input.is(TagUtil.Items.POT_INPUT_RECIPE)) {
                            shouldFilter = true;
                            break;
                        }
                    }

                    if (!shouldFilter && recipe.result().is(TagUtil.Items.POT_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                    }

                    if (!shouldFilter) {
                        List<EntryIngredient> inputs = ReiUtil.ofIngredients(recipe.getIngredients());
                        List<EntryIngredient> output = ReiUtil.ofItemStacks(recipe.getResultItem(RegistryAccess.EMPTY));
                        EntryIngredient carrier = recipe.carrier().isEmpty() ? EntryIngredient.empty() : ReiUtil.ofIngredient(recipe.carrier());

                        registry.add(new ReiPotRecipeCategory.PotRecipeDisplay(r.id(), inputs, output, carrier, recipe.stirFryCount()));
                    }
                });
    }
}