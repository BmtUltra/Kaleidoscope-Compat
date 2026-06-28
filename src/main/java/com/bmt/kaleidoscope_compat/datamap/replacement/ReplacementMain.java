package com.bmt.kaleidoscope_compat.datamap.replacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ReplacementMain(
        Optional<String> type,
        Optional<ResourceLocation> from,
        Optional<ResourceLocation> fromTag,
        Optional<ResourceLocation> toTag,
        Optional<ResourceLocation> to
) {
    public static final Codec<ReplacementMain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type").forGetter(ReplacementMain::type),
            ResourceLocation.CODEC.optionalFieldOf("from").forGetter(ReplacementMain::from),
            ResourceLocation.CODEC.optionalFieldOf("fromTag").forGetter(ReplacementMain::fromTag),
            ResourceLocation.CODEC.optionalFieldOf("toTag").forGetter(ReplacementMain::toTag),
            ResourceLocation.CODEC.optionalFieldOf("to").forGetter(ReplacementMain::to)
    ).apply(instance, ReplacementMain::new));
}