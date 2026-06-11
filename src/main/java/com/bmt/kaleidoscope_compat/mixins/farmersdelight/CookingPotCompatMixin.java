package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.api.event.StockpotMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.compat.farmersdelight.CookingPotCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(CookingPotCompat.class)
public class CookingPotCompatMixin {
    @Inject(method = "afterStockpotRecipeMatch", at = @At("TAIL"))
    private static void onAfterStockpotRecipeMatch(
            StockpotMatchRecipeEvent.Post event, CallbackInfo ci) {
        if (event.getOutput() != null &&
                event.getOutput().value().getResultItem(null)
                        .is(TagUtil.Items.ONLY_STOCKPOT_OUTPUT_RECIPE)) {
            event.setOutput(null);
        }
    }

    @Inject(method = "getTransformRecipeForJei", at = @At("HEAD"))
    private static void onGetTransformRecipeForJeiHead(
            Level level, List<RecipeHolder<StockpotRecipe>> recipes, CallbackInfo ci) {
        int originalSize = recipes.size();
        kaleidoscope_Compat_1_21_1_NeoForge$threadLocalSize.set(originalSize);
    }

    @Unique
    private static final ThreadLocal<Integer> kaleidoscope_Compat_1_21_1_NeoForge$threadLocalSize = new ThreadLocal<>();

    @Inject(method = "getTransformRecipeForJei", at = @At("TAIL"))
    private static void onGetTransformRecipeForJeiTail(
            Level level, List<RecipeHolder<StockpotRecipe>> recipes, CallbackInfo ci) {
        Integer originalSize = kaleidoscope_Compat_1_21_1_NeoForge$threadLocalSize.get();
        if (originalSize == null) {
            return;
        }
        kaleidoscope_Compat_1_21_1_NeoForge$threadLocalSize.remove();
        Iterator<RecipeHolder<StockpotRecipe>> iterator = recipes.listIterator(originalSize);
        while (iterator.hasNext()) {
            RecipeHolder<StockpotRecipe> holder = iterator.next();
            if (holder.value().result().is(TagUtil.Items.ONLY_STOCKPOT_OUTPUT_RECIPE)) {
                iterator.remove();
            }
        }
    }
}