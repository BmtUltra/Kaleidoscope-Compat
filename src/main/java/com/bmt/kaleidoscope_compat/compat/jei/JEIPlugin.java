package com.bmt.kaleidoscope_compat.compat.jei;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new WhirlwindBarbecueCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        List<RecipeHolder<CampfireCookingRecipe>> recipeHolders = Minecraft.getInstance().level
                .getRecipeManager()
                .getAllRecipesFor(RecipeType.CAMPFIRE_COOKING);

        List<CampfireCookingRecipe> campfireRecipes = recipeHolders.stream()
                .map(RecipeHolder::value)
                .collect(Collectors.toList());

        registration.addRecipes(WhirlwindBarbecueCategory.RECIPE_TYPE, campfireRecipes);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SHAWARMA_SPIT.get()),
                WhirlwindBarbecueCategory.RECIPE_TYPE
        );
    }
}