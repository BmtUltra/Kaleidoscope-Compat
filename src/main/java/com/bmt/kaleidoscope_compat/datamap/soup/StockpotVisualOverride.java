package com.bmt.kaleidoscope_compat.datamap.soup;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotVisuals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record StockpotVisualOverride(
        ResourceLocation recipeId,
        ResourceLocation cookingTexture,
        ResourceLocation finishedTexture,
        int cookingBubbleColor,
        int finishedBubbleColor
) {
    public static final Codec<StockpotVisualOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(StockpotVisualOverride::recipeId),
            ResourceLocation.CODEC.optionalFieldOf("cooking_texture", StockpotVisuals.DEFAULT_COOKING_TEXTURE).forGetter(StockpotVisualOverride::cookingTexture),
            ResourceLocation.CODEC.optionalFieldOf("finished_texture", StockpotVisuals.DEFAULT_FINISHED_TEXTURE).forGetter(StockpotVisualOverride::finishedTexture),
            Codec.INT.optionalFieldOf("cooking_bubble_color", StockpotVisuals.DEFAULT_COOKING_BUBBLE_COLOR).forGetter(StockpotVisualOverride::cookingBubbleColor),
            Codec.INT.optionalFieldOf("finished_bubble_color", StockpotVisuals.DEFAULT_FINISHED_BUBBLE_COLOR).forGetter(StockpotVisualOverride::finishedBubbleColor)
    ).apply(instance, StockpotVisualOverride::new));
}