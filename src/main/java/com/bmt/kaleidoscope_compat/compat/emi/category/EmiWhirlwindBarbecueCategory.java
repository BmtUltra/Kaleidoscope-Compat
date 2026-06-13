package com.bmt.kaleidoscope_compat.compat.emi.category;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;

import java.util.List;

public class EmiWhirlwindBarbecueCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "shawarma_spit"),
            EmiStack.of(ModBlocks.SHAWARMA_SPIT.get())
    );

    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeCompat.MOD_ID, "textures/gui/shawarma_spit.png"
    );
    public static final int WIDTH = 170;
    public static final int HEIGHT = 100;

    public EmiWhirlwindBarbecueCategory(ResourceLocation id, List<EmiIngredient> inputs, List<EmiStack> outputs) {
        super(CATEGORY, id, WIDTH, HEIGHT);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BG, 0, 0, WIDTH, HEIGHT, 0, 0);

        widgets.addSlot(inputs.getFirst(), 56, 35)
                .drawBack(false);

        widgets.addSlot(outputs.getFirst(), 122, 35)
                .drawBack(false)
                .recipeContext(this);
    }

    public static EmiWhirlwindBarbecueCategory fromRecipe(CampfireCookingRecipe recipe, ResourceLocation id) {
        List<EmiIngredient> inputs = List.of(EmiIngredient.of(recipe.getIngredients().getFirst()));
        List<EmiStack> outputs = List.of(EmiStack.of(recipe.getResultItem(null)));

        ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeCompat.MOD_ID,
                "/shawarma_spit/" + id.getNamespace() + "/" + id.getPath()
        );
        return new EmiWhirlwindBarbecueCategory(uniqueId, inputs, outputs);
    }
}