package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.rei.category.ReiChoppingBoardRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReiChoppingBoardRecipeCategory.class)
public class ReiChoppingBoardRecipeMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegisterDisplays(DisplayRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.CHOPPING_BOARD_RECIPE)
                .forEach(r -> {
                    ChoppingBoardRecipe recipe = r.value();

                    boolean shouldFilter = false;
                    for (ItemStack input : recipe.getIngredient().getItems()) {
                        if (input.is(TagUtil.Items.CHOPPING_BOARD_INPUT_RECIPE)) {
                            shouldFilter = true;
                            break;
                        }
                    }

                    if (!shouldFilter && recipe.getResult().is(TagUtil.Items.CHOPPING_BOARD_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                    }

                    if (!shouldFilter) {
                        List<EntryIngredient> input = ReiUtil.ofIngredients(recipe.getIngredients());
                        List<EntryIngredient> output = ReiUtil.ofItemStacks(recipe.getResult());

                        registry.add(new DefaultCustomDisplay(r, input, output) {
                            @Override
                            public me.shedaniel.rei.api.common.category.CategoryIdentifier<?> getCategoryIdentifier() {
                                return ReiChoppingBoardRecipeCategory.ID;
                            }
                        });
                    }
                });
    }
}