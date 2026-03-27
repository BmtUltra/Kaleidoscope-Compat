package com.bmt.kaleidoscope_compat.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagUtil {
    public static class Items {
        public static final TagKey<Item> STRAW_HATS =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "straw_hat"));

        public static final TagKey<Item> SMALL_BOTTLE =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "small_bottle"));

        public static final TagKey<Item> LARGE_BOTTLE =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "large_bottle"));

        public static final TagKey<Item> WINE =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("youkaisfeasts", "wine"));
    }

    public static class Blocks {
        /**
         * 镰刀可以破坏但没有掉落物的方块
         * 用于标记那些可以被镰刀破坏但不会产生掉落物的方块
         */
        public static final TagKey<Block> SICKLE_BREAKABLE_NO_DROPS =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_no_drops"));

        /**
         * 镰刀可以破坏且有掉落物的方块
         * 用于标记那些可以被镰刀破坏且会产生掉落物的方块
         */
        public static final TagKey<Block> SICKLE_BREAKABLE_WITH_DROPS =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "sickle_breakable_with_drops"));
    }
}