package com.bmt.kaleidoscope_compat.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagUtil {
    public static class Items {
        public static final TagKey<Item> STRAW_HATS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "straw_hat"));
        public static final TagKey<Item> SMALL_BOTTLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "small_bottle"));
        public static final TagKey<Item> LARGE_BOTTLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "large_bottle"));
        public static final TagKey<Item> WINE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("youkaisfeasts", "wine"));
        public static final TagKey<Item> HIDDEN_FROM_JEI = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "hidden_from_jei"));
        public static final TagKey<Item> FILTERED_FROM_FOOD_LIST = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "filtered_from_food_list"));
        public static final TagKey<Item> SCALE_DOWN_IN_CABINET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "scale_down_in_cabinet"));
        public static final TagKey<Item> REMOVED_FROM_ALL_CREATIVE_TABS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "removed_from_all_creative_tabs"));
        public static final TagKey<Item> LUNCH_BAG_BLACKLIST = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "lunch_bag_blacklist"));
    }

    public static class Blocks {
        public static final TagKey<Block> SICKLE_BREAKABLE_NO_DROPS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_no_drops"));
        public static final TagKey<Block> SICKLE_BREAKABLE_WITH_DROPS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_with_drops"));
        public static final TagKey<Block> CARRIER = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "carrier"));
    }

    public static class EntityTypes {
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> VITALITY_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("kaleidoscope_compat", "vitality_blacklist"));
    }
}