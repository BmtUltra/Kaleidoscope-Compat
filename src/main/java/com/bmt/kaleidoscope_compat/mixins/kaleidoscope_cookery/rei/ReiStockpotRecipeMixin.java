package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.rei;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.farmersdelight.FarmersDelightCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiStockpotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ReiStockpotRecipeCategory.class)
public class ReiStockpotRecipeMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        ci.cancel();

        List<RecipeHolder<StockpotRecipe>> list = new ArrayList<>(registry.getRecipeManager().getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE));
        FarmersDelightCompat.getTransformRecipeForJei(Minecraft.getInstance().level, list);

        list.forEach(r -> {
            StockpotRecipe recipe = r.value();

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
                List<EntryIngredient> inputs = ReiUtil.ofIngredients(recipe.getIngredients());
                List<EntryIngredient> output = ReiUtil.ofItemStacks(recipe.getResultItem(RegistryAccess.EMPTY));
                EntryIngredient carrier = recipe.carrier().isEmpty() ? EntryIngredient.empty() : ReiUtil.ofIngredient(recipe.carrier());

                ISoupBase soupBase = SoupBaseManager.getSoupBase(recipe.soupBase());
                if (soupBase == null) {
                    throw new RuntimeException("No soup found for " + recipe.soupBase());
                }
                EntryIngredient soupBaseEntry = ReiUtil.ofItemStack(soupBase.getDisplayStack());

                registry.add(new ReiStockpotRecipeCategory.StockpotRecipeDisplay(r.id(), inputs, output, carrier, soupBaseEntry));
            }
        });
    }
}