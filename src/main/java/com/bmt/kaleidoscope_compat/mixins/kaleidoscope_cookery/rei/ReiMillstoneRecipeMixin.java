package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.rei;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.create.CreateCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiMillstoneRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.google.common.collect.Lists;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReiMillstoneRecipeCategory.class)
public class ReiMillstoneRecipeMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        ci.cancel();

        List<RecipeHolder<MillstoneRecipe>> millstoneRecipes = Lists.newArrayList();
        millstoneRecipes.addAll(registry.getRecipeManager().getAllRecipesFor(ModRecipes.MILLSTONE_RECIPE));

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            CreateCompat.getTransformRecipeForSearch(level, millstoneRecipes);
        }

        millstoneRecipes.forEach(r -> {
            MillstoneRecipe recipe = r.value();

            boolean shouldFilter = false;
            for (ItemStack input : recipe.ingredient().getItems()) {
                if (input.is(TagUtil.Items.MILLSTONE_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter) {
                for (var output : recipe.results()) {
                    if (output.stack().is(TagUtil.Items.MILLSTONE_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                        break;
                    }
                }
            }

            if (!shouldFilter) {
                List<EntryIngredient> input = ReiUtil.ofIngredients(recipe.getIngredients());
                List<EntryIngredient> outputs = Lists.newArrayList();

                recipe.results().stream()
                        .filter(output -> !output.isEmpty())
                        .forEach(output -> {
                            EntryIngredient entry = ReiUtil.ofItemStack(output.stack());
                            outputs.add(entry);
                        });

                registry.add(new ReiMillstoneRecipeCategory.MillstoneRecipeDisplay(r.id(), input, outputs));
            }
        });
    }
}