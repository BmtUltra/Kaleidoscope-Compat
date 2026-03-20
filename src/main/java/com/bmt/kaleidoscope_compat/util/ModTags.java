package com.bmt.kaleidoscope_compat.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> STRAW_HATS =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "straw_hat"));
    }
}