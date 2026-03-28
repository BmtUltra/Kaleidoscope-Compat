package com.bmt.kaleidoscope_compat.compat.jei;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JeiPlugin
@SuppressWarnings("all")
public class Plugins implements IModPlugin {
    
    private static final ResourceLocation PLUGIN_ID = 
        ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "jei_plugin");
    
    private static final Set<Item> hiddenItems = new HashSet<>();
    private IJeiRuntime jeiRuntime;
    
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
    
    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new WhirlwindBarbecueCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }
    
    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registerBarbecueRecipes(registration);
    }
    
    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.SHAWARMA_SPIT.get()),
            WhirlwindBarbecueCategory.RECIPE_TYPE
        );
    }
    
    @Override
    public void registerAdvanced(@NotNull IAdvancedRegistration registration) {
        registration.addRecipeManagerPlugin(new RecipeHiding());
    }
    
    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
        updateHiddenItems();
        hideTaggedItems();
    }
    
    @Override
    public void onRuntimeUnavailable() {
        this.jeiRuntime = null;
        hiddenItems.clear();
    }
    
    private void registerBarbecueRecipes(@NotNull IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        List<RecipeHolder<CampfireCookingRecipe>> recipeHolders = Minecraft.getInstance().level
            .getRecipeManager()
            .getAllRecipesFor(RecipeType.CAMPFIRE_COOKING);
        
        List<CampfireCookingRecipe> campfireRecipes = recipeHolders.stream()
            .map(RecipeHolder::value)
            .collect(Collectors.toList());
        
        registration.addRecipes(WhirlwindBarbecueCategory.RECIPE_TYPE, campfireRecipes);
    }
    
    private void updateHiddenItems() {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        
        hiddenItems.clear();
        var itemRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(BuiltInRegistries.ITEM.key());

        for (Holder<Item> itemHolder : itemRegistry.getTagOrEmpty(TagUtil.Items.HIDDEN_FROM_JEI)) {
            hiddenItems.add(itemHolder.value());
        }
    }
    
    private void hideTaggedItems() {
        if (jeiRuntime == null || hiddenItems.isEmpty()) {
            return;
        }
        
        List<ItemStack> itemsToHide = new ArrayList<>();
        for (Item item : hiddenItems) {
            itemsToHide.add(new ItemStack(item));
        }
        
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, itemsToHide);
    }
    
    public static boolean isItemHidden(Item item) {
        return hiddenItems.contains(item);
    }
    
    public static Set<Item> getHiddenItems() {
        return new HashSet<>(hiddenItems);
    }
}