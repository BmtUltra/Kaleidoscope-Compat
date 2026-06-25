package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.emi;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiTeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
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

@Mixin(EmiTeapotRecipe.class)
public class EmiTeapotRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiTeapotRecipe.CATEGORY);
        registry.addWorkstation(EmiTeapotRecipe.CATEGORY, EmiStack.of(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.TEAPOT.get()));

        registry.getRecipeManager().getAllRecipesFor(ModRecipes.TEAPOT_RECIPE).forEach(r -> {
            TeapotRecipe value = r.value();

            boolean shouldFilter = false;
            for (ItemStack input : value.ingredient().getItems()) {
                if (input.is(TagUtil.Items.TEAPOT_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter && value.result().is(TagUtil.Items.TEAPOT_OUTPUT_RECIPE)) {
                shouldFilter = true;
            }

            if (!shouldFilter) {
                Fluid fluid = BuiltInRegistries.FLUID.get(value.teaFluid());
                Item bucket = fluid.getBucket();
                List<EmiIngredient> inputs = List.of(EmiIngredient.of(Arrays.stream(value.ingredient().getItems())
                        .map(stack -> EmiStack.of(stack.copyWithCount(value.ingredientCount())))
                        .toList()));
                List<EmiStack> outputs = List.of(EmiStack.of(value.result().copyWithCount(TeapotRecipe.OUTPUT_COUNT)));
                registry.addRecipe(new EmiTeapotRecipe(r.id(), inputs, outputs, bucket, value.time()));
            }
        });
    }
}