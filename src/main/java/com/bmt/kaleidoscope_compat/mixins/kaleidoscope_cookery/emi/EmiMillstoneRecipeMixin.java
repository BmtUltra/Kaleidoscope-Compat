package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.emi;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.create.CreateCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.emi.category.EmiMillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.google.common.collect.Lists;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EmiMillstoneRecipe.class)
@OnlyIn(Dist.CLIENT)
public class EmiMillstoneRecipeMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRegister(EmiRegistry registry, CallbackInfo ci) {
        ci.cancel();

        registry.addCategory(EmiMillstoneRecipe.CATEGORY);
        registry.addWorkstation(EmiMillstoneRecipe.CATEGORY, EmiStack.of(ModItems.MILLSTONE.get()));

        List<RecipeHolder<MillstoneRecipe>> millstoneRecipes = Lists.newArrayList();
        millstoneRecipes.addAll(registry.getRecipeManager().getAllRecipesFor(ModRecipes.MILLSTONE_RECIPE));

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            CreateCompat.getTransformRecipeForSearch(level, millstoneRecipes);
        }

        millstoneRecipes.forEach(r -> {
            MillstoneRecipe value = r.value();

            boolean shouldFilter = false;
            for (ItemStack input : value.ingredient().getItems()) {
                if (input.is(TagUtil.Items.MILLSTONE_INPUT_RECIPE)) {
                    shouldFilter = true;
                    break;
                }
            }

            if (!shouldFilter) {
                for (var output : value.results()) {
                    if (output.stack().is(TagUtil.Items.MILLSTONE_OUTPUT_RECIPE)) {
                        shouldFilter = true;
                        break;
                    }
                }
            }

            if (!shouldFilter) {
                List<EmiIngredient> inputs = value.getIngredients().stream().map(EmiIngredient::of).toList();
                List<EmiStack> outputs = Lists.newArrayList();

                value.results().stream()
                        .filter(output -> !output.isEmpty())
                        .forEach(output -> {
                            EmiStack emiStack = EmiStack.of(output.stack()).setChance(output.chance());
                            outputs.add(emiStack);
                        });

                registry.addRecipe(new EmiMillstoneRecipe(r.id(), inputs, outputs));
            }
        });
    }
}