package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.rei;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiTeapotRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(ReiTeapotRecipeCategory.class)
public class ReiTeapotRecipeMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.TEAPOT_RECIPE)
                .forEach(r -> {
                    TeapotRecipe recipe = r.value();

                    boolean shouldFilter = false;
                    for (ItemStack input : recipe.ingredient().getItems()) {
                        if (input.is(TagUtil.Items.TEAPOT_INPUT_RECIPE)) {
                            shouldFilter = true;
                            break;
                        }
                    }

                    if (!shouldFilter && recipe.result().is(TagUtil.Items.TEAPOT_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                    }

                    if (!shouldFilter) {
                        Fluid fluid = BuiltInRegistries.FLUID.get(recipe.teaFluid());
                        Item bucket = fluid.getBucket();
                        List<EntryIngredient> fluidInput = ReiUtil.ofItems(bucket);
                        List<EntryIngredient> inputs = List.of(EntryIngredient.of(Arrays.stream(recipe.ingredient().getItems())
                                .map(stack -> EntryStacks.of(stack.copyWithCount(recipe.ingredientCount())))
                                .toList()));
                        List<EntryIngredient> output = ReiUtil.ofItemStacks(recipe.result().copyWithCount(TeapotRecipe.OUTPUT_COUNT));

                        registry.add(new ReiTeapotRecipeCategory.TeapotRecipeDisplay(r.id(), fluidInput, inputs, output, recipe.time()));
                    }
                });
    }
}