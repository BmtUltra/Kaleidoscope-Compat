package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiStockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.farmersdelight.FarmersDelightCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.google.common.collect.Lists;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(EmiStockpotRecipe.class)
public class EmiStockpotRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiStockpotRecipe.CATEGORY);
        registry.addWorkstation(EmiStockpotRecipe.CATEGORY, EmiStack.of(ModItems.STOCKPOT.get()));
        registry.addWorkstation(EmiStockpotRecipe.CATEGORY, EmiStack.of(ModItems.STOCKPOT_LID.get()));

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE).forEach(r -> {
            kaleidoscope_Compat_1_21_1_NeoForge$registerRecipe(registry, r);
        });

        List<RecipeHolder<StockpotRecipe>> compatRecipes = Lists.newArrayList();
        FarmersDelightCompat.getTransformRecipeForJei(Minecraft.getInstance().level, compatRecipes);
        if (!compatRecipes.isEmpty()) {
            compatRecipes.forEach(r -> kaleidoscope_Compat_1_21_1_NeoForge$registerRecipe(registry, r));
        }
    }

    @Unique
    private static void kaleidoscope_Compat_1_21_1_NeoForge$registerRecipe(EmiRegistry registry, RecipeHolder<StockpotRecipe> holder) {
        StockpotRecipe recipe = holder.value();

        boolean shouldFilter = false;
        for (ItemStack input : recipe.getIngredients().stream()
                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                .toList()) {
            if (input.is(TagUtil.Items.STOCKPOT_INPUT_RECIPE)) {
                shouldFilter = true;
                break;
            }
        }

        if (!shouldFilter && recipe.result().is(TagUtil.Items.STOCKPOT_OUTPUT_RECIPE)) {
            shouldFilter = true;
        }

        if (!shouldFilter) {
            List<EmiIngredient> inputs = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
            List<EmiStack> outputs = List.of(EmiStack.of(recipe.getResultItem(RegistryAccess.EMPTY)));
            List<EmiIngredient> catalysts = recipe.carrier().isEmpty() ? List.of() : List.of(EmiIngredient.of(recipe.carrier()));

            ISoupBase soupBase = SoupBaseManager.getSoupBase(recipe.soupBase());
            if (soupBase == null) {
                throw new RuntimeException("No soup found for " + recipe.soupBase());
            }
            EmiStack soupBaseItem = EmiStack.of(soupBase.getDisplayStack());
            registry.addRecipe(new EmiStockpotRecipe(holder.id(), inputs, outputs, catalysts, soupBaseItem));
        }
    }
}