package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.create;

import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockpotBlockEntity.class)
public class StockpotBlockEntityArmRecipeMixin implements StockpotArmAutomation {
    @Unique
    private static final String KALEIDOSCOPE_COMPAT_ARM_RECIPE = "KaleidoscopeCompatArmRecipe";

    @Unique
    private RecipeItem.RecipeRecord kaleidoscopeCompat$storedRecipe;

    @Override
    public @Nullable RecipeItem.RecipeRecord kaleidoscopeCompat$getStoredRecipe() {
        return this.kaleidoscopeCompat$storedRecipe;
    }

    @Override
    public void kaleidoscopeCompat$setStoredRecipe(@Nullable RecipeItem.RecipeRecord recipe) {
        this.kaleidoscopeCompat$storedRecipe = recipe;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void kaleidoscopeCompat$saveStoredRecipe(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.kaleidoscopeCompat$storedRecipe != null) {
            tag.put(KALEIDOSCOPE_COMPAT_ARM_RECIPE,
                    RecipeItem.RecipeRecord.CODEC.encodeStart(NbtOps.INSTANCE, this.kaleidoscopeCompat$storedRecipe).getOrThrow());
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void kaleidoscopeCompat$loadStoredRecipe(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.kaleidoscopeCompat$storedRecipe = null;
        if (tag.contains(KALEIDOSCOPE_COMPAT_ARM_RECIPE)) {
            this.kaleidoscopeCompat$storedRecipe = RecipeItem.RecipeRecord.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, tag.get(KALEIDOSCOPE_COMPAT_ARM_RECIPE)))
                    .getOrThrow();
        }
    }
}
