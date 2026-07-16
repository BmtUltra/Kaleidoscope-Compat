package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.jei;

import com.bmt.kaleidoscope_compat.compat.spectrum.MillstoneAnvilCrushingCompat;
import com.bmt.kaleidoscope_compat.config.category.SpectrumCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.jei.category.MillstoneRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MillstoneRecipeCategory.class)
public class MillstoneRecipeCategoryMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private static void onGetRecipes(CallbackInfoReturnable<List<RecipeHolder<MillstoneRecipe>>> cir) {
        if (ModList.get().isLoaded("spectrum") && SpectrumCategory.spectrumMillstoneAnvilCrushingEnabled) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                List<RecipeHolder<MillstoneRecipe>> originalRecipes = cir.getReturnValue();
                List<RecipeHolder<MillstoneRecipe>> mergedRecipes = new ArrayList<>(originalRecipes);
                MillstoneAnvilCrushingCompat.getTransformRecipeForJei(level, mergedRecipes);
                cir.setReturnValue(mergedRecipes);
            }
        }
    }
}