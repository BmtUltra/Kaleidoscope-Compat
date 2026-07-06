package com.bmt.kaleidoscope_compat.datamap.replacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ReplacementMain(
        Optional<String> type,
        List<ResourceLocation> from,
        Optional<ResourceLocation> to
) {
    public static final Codec<ReplacementMain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type").forGetter(ReplacementMain::type),
            ResourceLocation.CODEC.listOf().optionalFieldOf("from", Collections.emptyList()).forGetter(ReplacementMain::from),
            ResourceLocation.CODEC.optionalFieldOf("to").forGetter(ReplacementMain::to)
    ).apply(instance, ReplacementMain::new));

    public boolean hasFromTag() {
        return from.stream().anyMatch(id -> id.getPath().contains("#"));
    }

    public boolean isToTag() {
        return to.map(id -> id.getPath().contains("#")).orElse(false);
    }

    public List<ResourceLocation> getFromTagIds() {
        return from.stream()
                .filter(id -> id.getPath().contains("#"))
                .map(id -> ResourceLocation.parse(id.getPath().replace("#", "")))
                .toList();
    }

    public List<ResourceLocation> getFromItemIds() {
        return from.stream()
                .filter(id -> !id.getPath().contains("#"))
                .toList();
    }

    public Optional<ResourceLocation> getToTagId() {
        return to.filter(id -> id.getPath().contains("#"))
                .map(id -> ResourceLocation.parse(id.getPath().replace("#", "")));
    }
}