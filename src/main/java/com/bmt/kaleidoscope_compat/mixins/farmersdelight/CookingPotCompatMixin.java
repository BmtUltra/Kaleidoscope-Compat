package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.github.ysbbbbbb.kaleidoscopecookery.compat.farmersdelight.CookingPotCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@Mixin(CookingPotCompat.class)
public class CookingPotCompatMixin {
    @Unique
    private static final String KALEIDOSCOPE_COOKERY_MOD_ID = "kaleidoscope_cookery";

    @Inject(method = "transformRecipe", at = @At("RETURN"), cancellable = true)
    private static void onTransformRecipe(RecipeHolder<CookingPotRecipe> holder, Level level, CallbackInfoReturnable<RecipeHolder<StockpotRecipe>> cir) {
        RecipeHolder<StockpotRecipe> original = cir.getReturnValue();
        ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                KALEIDOSCOPE_COOKERY_MOD_ID, holder.id().getPath()
        );
        cir.setReturnValue(new RecipeHolder<>(newId, original.value()));
    }
}