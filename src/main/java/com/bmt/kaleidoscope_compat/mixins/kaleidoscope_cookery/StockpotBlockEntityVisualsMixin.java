package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.datamap.soup.StockpotVisualOverride;
import com.bmt.kaleidoscope_compat.datamap.soup.StockpotVisualOverrideManager;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotVisuals;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockpotBlockEntity.class)
public abstract class StockpotBlockEntityVisualsMixin {

    @Shadow(remap = false)
    private ResourceLocation recipeId;

    @Shadow(remap = false)
    public @org.jetbrains.annotations.Nullable StockpotVisuals visuals;

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        kaleidoscope_Compat_1_21_1_NeoForge$applyVisualOverride();
    }

    @Inject(method = "setRecipe", at = @At("RETURN"), remap = false)
    private void onSetRecipe(net.minecraft.world.level.Level levelIn, CallbackInfo ci) {
        kaleidoscope_Compat_1_21_1_NeoForge$applyVisualOverride();
    }

    @Unique
    private void kaleidoscope_Compat_1_21_1_NeoForge$applyVisualOverride() {
        if (this.recipeId != null) {
            StockpotVisualOverride override = StockpotVisualOverrideManager.getOverride(this.recipeId);
            if (override != null) {
                this.visuals = new StockpotVisuals(
                        override.cookingTexture(),
                        override.finishedTexture(),
                        override.cookingBubbleColor(),
                        override.finishedBubbleColor()
                );
            }
        }
    }
}