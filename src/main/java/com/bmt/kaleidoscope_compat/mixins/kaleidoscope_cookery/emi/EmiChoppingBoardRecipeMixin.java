package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.emi;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EmiChoppingBoardRecipe.class)
public class EmiChoppingBoardRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiChoppingBoardRecipe.CATEGORY);
        registry.addWorkstation(EmiChoppingBoardRecipe.CATEGORY, EmiStack.of(ModItems.CHOPPING_BOARD.get()));
        registry.addWorkstation(EmiChoppingBoardRecipe.CATEGORY, EmiIngredient.of(TagMod.KITCHEN_KNIFE));

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.CHOPPING_BOARD_RECIPE).forEach(recipeHolder -> {
            ChoppingBoardRecipe r = recipeHolder.value();

            boolean shouldFilter = false;
            for (ItemStack input : r.getIngredient().getItems()) {
                if (input.is(TagUtil.Items.CHOPPING_BOARD_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && r.getResult().is(TagUtil.Items.CHOPPING_BOARD_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                List<EmiIngredient> inputs = r.getIngredients().stream().map(EmiIngredient::of).toList();
                List<EmiStack> outputs = List.of(EmiStack.of(r.getResultItem(RegistryAccess.EMPTY)));
                registry.addRecipe(new EmiChoppingBoardRecipe(recipeHolder.id(), inputs, outputs));
            }
        });
    }
}