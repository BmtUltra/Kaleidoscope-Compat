package com.bmt.kaleidoscope_compat.compat.emi;

import com.bmt.kaleidoscope_compat.compat.emi.category.EmiWhirlwindBarbecueCategory;
import com.bmt.kaleidoscope_compat.config.category.OtherCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

@EmiEntrypoint
public class EmiPlugin implements dev.emi.emi.api.EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!OtherCategory.jeiCompatEnabled) return;
        registry.addCategory(EmiWhirlwindBarbecueCategory.CATEGORY);
        registry.addWorkstation(EmiWhirlwindBarbecueCategory.CATEGORY, EmiStack.of(ModBlocks.SHAWARMA_SPIT.get()));
        registerBarbecueRecipes(registry);
    }

    private void registerBarbecueRecipes(EmiRegistry registry) {
        if (Minecraft.getInstance().level == null) return;

        List<RecipeHolder<CampfireCookingRecipe>> recipeHolders = Minecraft.getInstance().level
                .getRecipeManager()
                .getAllRecipesFor(RecipeType.CAMPFIRE_COOKING);

        for (RecipeHolder<CampfireCookingRecipe> holder : recipeHolders) {
            CampfireCookingRecipe recipe = holder.value();
            EmiWhirlwindBarbecueCategory emiRecipe = EmiWhirlwindBarbecueCategory.fromRecipe(recipe, holder.id());
            registry.addRecipe(emiRecipe);
        }
    }
}