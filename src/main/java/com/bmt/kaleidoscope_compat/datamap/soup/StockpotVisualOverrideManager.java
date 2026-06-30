package com.bmt.kaleidoscope_compat.datamap.soup;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StockpotVisualOverrideManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, StockpotVisualOverride> OVERRIDES = Maps.newHashMap();
    private static final String DIRECTORY = "soup";

    public StockpotVisualOverrideManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        OVERRIDES.clear();

        objects.forEach((fileId, json) -> {
            if (json.isJsonArray()) {
                json.getAsJsonArray().forEach(element -> parseAndRegister(fileId, element));
            } else if (json.isJsonObject()) {
                parseAndRegister(fileId, json);
            }
        });
    }

    @SuppressWarnings("unused")
    private void parseAndRegister(ResourceLocation fileId, JsonElement element) {
        StockpotVisualOverride.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> {})
                .ifPresent(override -> OVERRIDES.put(override.recipeId(), override));
    }

    public static StockpotVisualOverride getOverride(ResourceLocation recipeId) {
        return OVERRIDES.get(recipeId);
    }
}