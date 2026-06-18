package com.bmt.kaleidoscope_compat.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record StockpotVisualOverride(
        ResourceLocation recipeId,
        ResourceLocation cookingTexture,
        ResourceLocation finishedTexture,
        Integer cookingBubbleColor,
        Integer finishedBubbleColor
) {
    public static final Codec<StockpotVisualOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(StockpotVisualOverride::recipeId),
            ResourceLocation.CODEC.optionalFieldOf("cooking_texture", null).forGetter(o -> o.cookingTexture),
            ResourceLocation.CODEC.optionalFieldOf("finished_texture", null).forGetter(o -> o.finishedTexture),
            Codec.INT.optionalFieldOf("cooking_bubble_color", null).forGetter(o -> o.cookingBubbleColor),
            Codec.INT.optionalFieldOf("finished_bubble_color", null).forGetter(o -> o.finishedBubbleColor)
    ).apply(instance, StockpotVisualOverride::new));
}