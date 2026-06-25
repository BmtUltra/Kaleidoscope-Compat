package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.emi;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiPotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(EmiPotRecipe.class)
public class EmiPotRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiPotRecipe.CATEGORY);
        registry.addWorkstation(EmiPotRecipe.CATEGORY, EmiStack.of(ModItems.POT.get()));
        registry.addWorkstation(EmiPotRecipe.CATEGORY, EmiStack.of(ModItems.KITCHEN_SHOVEL.get()));

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.POT_RECIPE).forEach(recipeHolder -> {
            PotRecipe r = recipeHolder.value();

            boolean shouldFilter = false;
            for (ItemStack input : r.getIngredients().stream()
                    .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                    .toList()) {
                if (input.is(TagUtil.Items.POT_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && r.result().is(TagUtil.Items.POT_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                List<EmiIngredient> inputs = r.getIngredients().stream().map(EmiIngredient::of).toList();
                List<EmiStack> outputs = List.of(EmiStack.of(r.getResultItem(RegistryAccess.EMPTY)));
                List<EmiIngredient> catalysts = r.carrier().isEmpty() ? List.of() : List.of(EmiIngredient.of(r.carrier()));

                registry.addRecipe(new EmiPotRecipe(recipeHolder.id(), inputs, outputs, catalysts, r.stirFryCount()));
            }
        });
    }
}