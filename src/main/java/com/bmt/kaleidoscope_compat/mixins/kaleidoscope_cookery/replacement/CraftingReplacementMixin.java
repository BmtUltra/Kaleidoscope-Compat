package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class CraftingReplacementMixin {
    @Unique
    private static final String REPLACEMENT_TYPE = "crafting";

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD")
    )
    private void onApply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                         ProfilerFiller profiler, CallbackInfo ci) {
            ReplacementManager.loadData(resourceManager);

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredients(json);
            }
        }
    }

    @Unique
    private void kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredients(JsonObject json) {
        if (json.has("key")) {
            JsonObject keys = json.getAsJsonObject("key");
            for (String key : keys.keySet()) {
                JsonElement value = keys.get(key);
                if (value.isJsonObject()) {
                    kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredient(value.getAsJsonObject());
                }
            }
        }

        if (json.has("ingredients")) {
            var ingredients = json.getAsJsonArray("ingredients");
            for (int i = 0; i < ingredients.size(); i++) {
                JsonElement ingredient = ingredients.get(i);
                if (ingredient.isJsonObject()) {
                    kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredient(ingredient.getAsJsonObject());
                }
            }
        }
    }

    @Unique
    private void kaleidoscope_Compat_1_21_1_NeoForge$replaceIngredient(JsonObject ingredient) {
        if (ingredient.has("item")) {
            String itemId = ingredient.get("item").getAsString();
            ResourceLocation from = ResourceLocation.parse(itemId);

            ResourceLocation tagReplacement = ReplacementManager.getTagReplacement(REPLACEMENT_TYPE, from);
            if (tagReplacement != null) {
                ingredient.remove("item");
                ingredient.addProperty("tag", tagReplacement.toString());
                return;
            }

            ResourceLocation itemReplacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, from);
            if (itemReplacement != null) {
                ingredient.addProperty("item", itemReplacement.toString());
            }
        }

        if (ingredient.has("tag")) {
            String tagId = ingredient.get("tag").getAsString();
            ResourceLocation tagLocation = ResourceLocation.parse(tagId);
            TagKey<Item> tagKey = ItemTags.create(tagLocation);

            for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                Item item = entry.getValue();
                if (item != Items.AIR) {
                    ResourceLocation itemId = entry.getKey().location();
                    BuiltInRegistries.ITEM.getHolder(itemId)
                            .ifPresent(holder -> {
                                if (holder.is(tagKey)) {
                                    ResourceLocation itemReplacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
                                    if (itemReplacement != null) {
                                        ingredient.remove("tag");
                                        ingredient.addProperty("item", itemReplacement.toString());
                                    }

                                    ResourceLocation tagReplacement = ReplacementManager.getTagReplacement(REPLACEMENT_TYPE, itemId);
                                    if (tagReplacement != null) {
                                        ingredient.remove("tag");
                                        ingredient.addProperty("tag", tagReplacement.toString());
                                    }
                                }
                            });
                }
            }
        }
    }
}