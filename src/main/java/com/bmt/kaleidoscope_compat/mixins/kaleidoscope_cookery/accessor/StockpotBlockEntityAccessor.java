package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StockpotBlockEntity.class)
public interface StockpotBlockEntityAccessor {
    @Accessor("recipeId")
    ResourceLocation kaleidoscopeCompat$getRecipeId();

    @Accessor("recipeId")
    @Mutable
    void kaleidoscopeCompat$setRecipeId(ResourceLocation recipeId);

    @Accessor("soupBaseId")
    ResourceLocation kaleidoscopeCompat$getSoupBaseId();

    @Accessor("soupBaseId")
    @Mutable
    void kaleidoscopeCompat$setSoupBaseId(ResourceLocation soupBaseId);

    @Accessor("result")
    @Mutable
    void kaleidoscopeCompat$setResult(ItemStack result);

    @Accessor("currentTick")
    @Mutable
    void kaleidoscopeCompat$setCurrentTick(int currentTick);

    @Accessor("takeoutCount")
    @Mutable
    void kaleidoscopeCompat$setTakeoutCount(int takeoutCount);
}