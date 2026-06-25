package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.emi;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiSteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
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

@Mixin(EmiSteamerRecipe.class)
public class EmiSteamerRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiSteamerRecipe.CATEGORY);
        registry.addWorkstation(EmiSteamerRecipe.CATEGORY, EmiStack.of(ModItems.STEAMER.get()));
        registry.addWorkstation(EmiSteamerRecipe.CATEGORY, EmiIngredient.of(TagMod.KITCHEN_KNIFE));

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.STEAMER_RECIPE).forEach(holder -> {
            SteamerRecipe recipe = holder.value();

            boolean shouldFilter = false;
            for (ItemStack input : recipe.getIngredient().getItems()) {
                if (input.is(TagUtil.Items.STEAMER_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && recipe.getResult().is(TagUtil.Items.STEAMER_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                List<EmiIngredient> inputs = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
                List<EmiStack> outputs = List.of(EmiStack.of(recipe.getResultItem(RegistryAccess.EMPTY)));
                registry.addRecipe(new EmiSteamerRecipe(holder.id(), inputs, outputs));
            }
        });
    }
}