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
                ResourceLocation cookingTexture = override.cookingTexture() != null ? 
                    override.cookingTexture() : (this.visuals != null ? this.visuals.cookingTexture() : StockpotVisuals.DEFAULT_COOKING_TEXTURE);
                ResourceLocation finishedTexture = override.finishedTexture() != null ? 
                    override.finishedTexture() : (this.visuals != null ? this.visuals.finishedTexture() : StockpotVisuals.DEFAULT_FINISHED_TEXTURE);
                int cookingBubbleColor = override.cookingBubbleColor() != null ? 
                    override.cookingBubbleColor() : (this.visuals != null ? this.visuals.cookingBubbleColor() : StockpotVisuals.DEFAULT_COOKING_BUBBLE_COLOR);
                int finishedBubbleColor = override.finishedBubbleColor() != null ? 
                    override.finishedBubbleColor() : (this.visuals != null ? this.visuals.finishedBubbleColor() : StockpotVisuals.DEFAULT_FINISHED_BUBBLE_COLOR);
                this.visuals = new StockpotVisuals(cookingTexture, finishedTexture, cookingBubbleColor, finishedBubbleColor);
            }
        }
    }
}