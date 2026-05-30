package com.bmt.kaleidoscope_compat.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("all")
public class TagUtil {
    public static class Items {
        public static final TagKey<Item> STRAW_HATS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "straw_hat"));
        public static final TagKey<Item> SMALL_BOTTLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "small_bottle"));
        public static final TagKey<Item> LARGE_BOTTLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "large_bottle"));
        public static final TagKey<Item> WINE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("youkaisfeasts", "wine"));
        public static final TagKey<Item> BOTTLE_OIL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "bottle_oil"));
        public static final TagKey<Item> BUCKET_OIL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "bucket_oil"));
        public static final TagKey<Item> SCALE_DOWN_IN_CABINET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "scale_down_in_cabinet"));
        public static final TagKey<Item> POT_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "pot_input_recipe"));
        public static final TagKey<Item> POT_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "pot_output_recipe"));
        public static final TagKey<Item> CHOPPING_BOARD_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "chopping_board_input_recipe"));
        public static final TagKey<Item> CHOPPING_BOARD_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "chopping_board_output_recipe"));
        public static final TagKey<Item> MILLSTONE_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "millstone_input_recipe"));
        public static final TagKey<Item> MILLSTONE_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "millstone_output_recipe"));
        public static final TagKey<Item> STEAMER_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "steamer_input_recipe"));
        public static final TagKey<Item> STEAMER_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "steamer_output_recipe"));
        public static final TagKey<Item> STOCKPOT_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "stockpot_input_recipe"));
        public static final TagKey<Item> STOCKPOT_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "stockpot_output_recipe"));
        public static final TagKey<Item> TEAPOT_INPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "teapot_input_recipe"));
        public static final TagKey<Item> TEAPOT_OUTPUT_RECIPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "teapot_output_recipe"));
        public static final TagKey<Item> UNITED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "united"));
    }

    public static class Blocks {
        public static final TagKey<Block> SICKLE_BREAKABLE_NO_DROPS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_no_drops"));
        public static final TagKey<Block> SICKLE_BREAKABLE_WITH_DROPS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_with_drops"));
        public static final TagKey<Block> CARRIER = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "carrier"));
    }
}