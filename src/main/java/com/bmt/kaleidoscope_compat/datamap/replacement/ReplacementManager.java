package com.bmt.kaleidoscope_compat.datamap.replacement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplacementManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Map<ResourceLocation, ResourceLocation>> ITEM_REPLACEMENTS = new HashMap<>();
    private static final Map<String, Map<ResourceLocation, ResourceLocation>> ITEM_TO_TAG_REPLACEMENTS = new HashMap<>();

    private static final String DIRECTORY = "replacement";
    private static final String DEFAULT_TYPE = "crafting";

    private static final List<String> ALL_TYPES = List.of("crafting", "pot", "stockpot", "flexpot", "flexstockpot", "croploot", "chestloot", "entityloot","villager");

    public ReplacementManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        clearAllReplacements();

        objects.forEach((fileId, json) -> {
            if (json.isJsonArray()) {
                json.getAsJsonArray().forEach(element -> parseAndRegister(fileId, element));
            } else if (json.isJsonObject()) {
                parseAndRegister(fileId, json);
            }
        });
    }

    public static void loadData(ResourceManager resourceManager) {
        clearAllReplacements();
        Map<ResourceLocation, JsonElement> resources = new HashMap<>();

        resourceManager.listResources(DIRECTORY, location -> location.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (InputStream inputStream = resource.open();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        JsonElement json = JsonParser.parseReader(reader);
                        resources.put(location, json);
                    } catch (IOException ignored) {
                    }
                });

        resources.forEach((fileId, json) -> {
            if (json.isJsonArray()) {
                json.getAsJsonArray().forEach(element -> parseAndRegisterStatic(fileId, element));
            } else if (json.isJsonObject()) {
                parseAndRegisterStatic(fileId, json);
            }
        });
    }

    private static void clearAllReplacements() {
        ITEM_REPLACEMENTS.clear();
        ITEM_TO_TAG_REPLACEMENTS.clear();
    }

    private static List<String> parseTypes(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return List.of(DEFAULT_TYPE);
        }

        if (typeStr.equals("all")) {
            return ALL_TYPES;
        }

        return Arrays.stream(typeStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @SuppressWarnings("unused")
    private static void parseAndRegisterStatic(ResourceLocation fileId, JsonElement element) {
        ReplacementMain.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> {})
                .ifPresent(replacement -> {
                    List<String> types = parseTypes(replacement.type().orElse(DEFAULT_TYPE));
                    for (String type : types) {
                        if (replacement.hasFromTag()) {
                            for (ResourceLocation tagId : replacement.getFromTagIds()) {
                                TagKey<Item> tagKey = ItemTags.create(tagId);
                                for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                                    Item item = entry.getValue();
                                    if (item != Items.AIR) {
                                        ResourceLocation itemId = entry.getKey().location();
                                        BuiltInRegistries.ITEM.getHolder(itemId)
                                                .ifPresent(holder -> {
                                                    if (holder.is(tagKey)) {
                                                        registerReplacementStatic(type, itemId, replacement);
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                        for (ResourceLocation fromId : replacement.getFromItemIds()) {
                            registerReplacementStatic(type, fromId, replacement);
                        }
                    }
                });
    }

    private static void registerReplacementStatic(String type, ResourceLocation sourceItem, ReplacementMain replacement) {
        if (replacement.isToTag()) {
            replacement.getToTagId().ifPresent(tagId -> ITEM_TO_TAG_REPLACEMENTS.computeIfAbsent(type, k -> new HashMap<>())
                    .put(sourceItem, tagId));
        } else if (replacement.to().isPresent()) {
            ITEM_REPLACEMENTS.computeIfAbsent(type, k -> new HashMap<>())
                    .put(sourceItem, replacement.to().get());
        }
    }

    private void parseAndRegister(ResourceLocation fileId, JsonElement element) {
        parseAndRegisterStatic(fileId, element);
    }

    public static ResourceLocation getItemReplacement(String type, ResourceLocation from) {
        Map<ResourceLocation, ResourceLocation> typeReplacements = ITEM_REPLACEMENTS.get(type);
        if (typeReplacements != null) {
            return typeReplacements.get(from);
        }
        return null;
    }

    public static ResourceLocation getTagReplacement(String type, ResourceLocation from) {
        Map<ResourceLocation, ResourceLocation> typeReplacements = ITEM_TO_TAG_REPLACEMENTS.get(type);
        if (typeReplacements != null) {
            return typeReplacements.get(from);
        }
        return null;
    }
}