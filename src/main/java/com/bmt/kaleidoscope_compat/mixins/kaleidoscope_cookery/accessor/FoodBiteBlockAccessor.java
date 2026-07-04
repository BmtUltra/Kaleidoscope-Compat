package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBiteBlock;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodBiteBlock.class)
public interface FoodBiteBlockAccessor {
    @Accessor("foodProperties")
    FoodProperties getFoodProperties();
}