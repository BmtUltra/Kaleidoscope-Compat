package com.bmt.kaleidoscope_compat.compat.solcarrot;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class FoodListFilter {

    private static final Set<Item> filteredItems = new HashSet<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        filteredItems.clear();

        var itemRegistry = BuiltInRegistries.ITEM;
        for (Holder<Item> itemHolder : itemRegistry.getTagOrEmpty(TagUtil.Items.UNITED)) {
            filteredItems.add(itemHolder.value());
        }
        initialized = true;
    }

    public static boolean shouldFilter(ItemStack itemStack) {
        if (!initialized) {
            initialize();
        }

        Item item = itemStack.getItem();
        return filteredItems.contains(item);
    }

    public static boolean shouldFilter(Item item) {
        if (!initialized) {
            initialize();
        }

        return filteredItems.contains(item);
    }

    public static void reload() {
        initialized = false;
        initialize();
    }

    public static Set<Item> getFilteredItems() {
        if (!initialized) {
            initialize();
        }
        return new HashSet<>(filteredItems);
    }
}