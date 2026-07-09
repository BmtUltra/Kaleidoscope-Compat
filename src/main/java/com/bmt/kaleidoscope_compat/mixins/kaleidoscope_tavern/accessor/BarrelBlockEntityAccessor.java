package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern.accessor;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BarrelBlockEntity.class)
public interface BarrelBlockEntityAccessor {

    @Accessor("brewLevel")
    void setBrewLevel(int level);

    @Accessor("brewTime")
    void setBrewTime(int time);

    @Accessor("recipeId")
    void setRecipeId(ResourceLocation recipeId);
}