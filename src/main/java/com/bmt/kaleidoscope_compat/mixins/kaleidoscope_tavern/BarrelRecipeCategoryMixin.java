package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern;

import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.BarrelRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BarrelRecipeCategory.class)
public class BarrelRecipeCategoryMixin {

    @Inject(
            method = "getRecipes",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void kc$addVineryBarrelRecipes(CallbackInfoReturnable<List<RecipeHolder<BarrelRecipe>>> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<RecipeHolder<BarrelRecipe>> recipes = Lists.newArrayList(cir.getReturnValue());
        cir.setReturnValue(recipes);
    }
}