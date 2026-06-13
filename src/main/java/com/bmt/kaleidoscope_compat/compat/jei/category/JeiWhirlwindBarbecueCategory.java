package com.bmt.kaleidoscope_compat.compat.jei.category;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("removal")
public class JeiWhirlwindBarbecueCategory implements IRecipeCategory<CampfireCookingRecipe> {
    
    public static final RecipeType<CampfireCookingRecipe> RECIPE_TYPE =
        RecipeType.create(KaleidoscopeCompat.MOD_ID, "shawarma_spit", CampfireCookingRecipe.class);
    
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, 
            "textures/gui/shawarma_spit.png");
    
    private final IDrawable background;
    private final IDrawable icon;
    
    public JeiWhirlwindBarbecueCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 170, 100);

        this.icon = guiHelper.createDrawableItemStack(
            new ItemStack(
                    Objects.requireNonNull(ModBlocks.SHAWARMA_SPIT.get())
            )
        );
    }
    
    @Override
    public @NotNull RecipeType<CampfireCookingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
    
    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.kaleidoscope_compat.shawarma_spit");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }
    
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CampfireCookingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 57, 36)
            .addIngredients(recipe.getIngredients().getFirst());
        
        builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 36)
            .addItemStack(recipe.getResultItem(null));
    }
}