package com.bmt.kaleidoscope_compat.compat.rei.category;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReiWhirlwindBarbecueCategory implements DisplayCategory<ReiWhirlwindBarbecueCategory.WhirlwindBarbecueDisplay> {
    public static final CategoryIdentifier<WhirlwindBarbecueDisplay> ID = CategoryIdentifier.of(
            KaleidoscopeCompat.MOD_ID, "shawarma_spit"
    );

    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeCompat.MOD_ID, "textures/gui/shawarma_spit.png"
    );

    private static final Component TITLE = Component.translatable("jei.category.kaleidoscope_compat.shawarma_spit");

    public static final int WIDTH = 170;
    public static final int HEIGHT = 100;

    @Override
    public CategoryIdentifier<WhirlwindBarbecueDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(WhirlwindBarbecueDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        int startX = bounds.x;
        int startY = bounds.y;

        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createTexturedWidget(BG, startX, startY, 0, 0, WIDTH, HEIGHT));

        widgets.add(Widgets.createSlot(new Point(startX + 57, startY + 36))
                .entries(display.getInputEntries().getFirst())
                .disableBackground()
                .markInput());

        widgets.add(Widgets.createSlot(new Point(startX + 123, startY + 36))
                .entries(display.getOutputEntries().getFirst())
                .disableBackground()
                .markOutput());

        return widgets;
    }

    @Override
    public int getDisplayWidth(WhirlwindBarbecueDisplay display) {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(new ItemStack(ModBlocks.SHAWARMA_SPIT.get()));
    }

    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new ReiWhirlwindBarbecueCategory());
        registry.addWorkstations(ReiWhirlwindBarbecueCategory.ID,
                EntryStacks.of(new ItemStack(ModBlocks.SHAWARMA_SPIT.get()))
        );
    }

    public static void registerDisplays(DisplayRegistry registry) {
        if (Minecraft.getInstance().level == null) return;

        List<RecipeHolder<CampfireCookingRecipe>> recipeHolders = Minecraft.getInstance().level
                .getRecipeManager()
                .getAllRecipesFor(RecipeType.CAMPFIRE_COOKING);

        for (RecipeHolder<CampfireCookingRecipe> holder : recipeHolders) {
            CampfireCookingRecipe recipe = holder.value();

            Ingredient ingredient = recipe.getIngredients().getFirst();
            List<EntryStack<?>> inputStacks = Arrays.stream(ingredient.getItems())
                    .map(EntryStacks::of)
                    .collect(Collectors.toList());
            List<EntryIngredient> inputs = List.of(EntryIngredient.of(inputStacks));

            List<EntryStack<?>> outputStacks = List.of(EntryStacks.of(recipe.getResultItem(RegistryAccess.EMPTY)));
            List<EntryIngredient> outputs = List.of(EntryIngredient.of(outputStacks));

            registry.add(new WhirlwindBarbecueDisplay(holder.id(), inputs, outputs));
        }
    }

    public static class WhirlwindBarbecueDisplay extends BasicDisplay {
        public WhirlwindBarbecueDisplay(ResourceLocation location, List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
            super(inputs, outputs, Optional.of(location));
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ID;
        }
    }
}