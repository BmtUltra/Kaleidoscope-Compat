package com.bmt.kaleidoscope_compat.compat.thirst;

import com.bmt.kaleidoscope_compat.config.category.OtherCategory;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class ThirstCompat {

    private static final Map<String, int[]> SOUP_ITEMS = new HashMap<>();

    static {
        SOUP_ITEMS.put("kaleidoscope_cookery:pork_bone_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_cookery:seafood_miso_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:fearsome_thick_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_cookery:lamb_and_radish_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:braised_beef_with_potatoes", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_cookery:wild_mushroom_rabbit_soup", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_cookery:tomato_beef_brisket_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:pufferfish_soup", new int[]{2, 4});
        SOUP_ITEMS.put("kaleidoscope_cookery:borscht", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_cookery:beef_meatball_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_cookery:chicken_and_mushroom_stew", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:donkey_soup", new int[]{6, 8});

        SOUP_ITEMS.put("kaleidoscope_nether:blaze_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_nether:wither_bone_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_nether:star_stew", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:soul_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:poisonous_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:magma_cream_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:glowing_soup", new int[]{3, 5});

        SOUP_ITEMS.put("kaleidoscope_end:dragon_breath_chorus_soup", new int[]{6, 8});

        SOUP_ITEMS.put("kaleidoscope_chinesefood:seaweed_egg_drop_soup", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_chinesefood:tomato_egg_drop_soup", new int[]{6, 8});
    }

    private static final int[] TEA_HYDRATION = new int[]{3, 5};
    private static final int[] DRINK_HYDRATION = new int[]{2, 4};
    private static final int[] COCKTAIL_HYDRATION = new int[]{1, 3};

    private static Class<?> drinkBlockItemClass;
    private static Class<?> cocktailBlockItemClass;

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("thirst") || !OtherCategory.thirstCompatEnabled) {
            return;
        }
        event.enqueueWork(ThirstCompat::registerItems);
    }

    private static void registerItems() {
        boolean isNewVersion = isNewThirstVersion();

        try {
            drinkBlockItemClass = Class.forName("com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            cocktailBlockItemClass = Class.forName("com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem");
        } catch (ClassNotFoundException ignored) {
        }

        for (Map.Entry<String, int[]> entry : SOUP_ITEMS.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
            if (item != Items.AIR) {
                int[] values = entry.getValue();
                registerItem(item, values[0], values[1], isNewVersion);
            }
        }

        registerTeaItems(isNewVersion);
        registerDrinkBlockItems(isNewVersion);
        registerCocktailBlockItems(isNewVersion);
    }

    private static void registerTeaItems(boolean isNewVersion) {
        for (Map.Entry<ResourceLocation, TeacupRegistry.TeacupData> entry : TeacupRegistry.TEACUP_DATA_MAP.entrySet()) {
            ResourceLocation teaId = entry.getKey();
            Item item = BuiltInRegistries.ITEM.get(teaId);
            if (item != Items.AIR) {
                registerItem(item, TEA_HYDRATION[0], TEA_HYDRATION[1], isNewVersion);
            }
        }
    }

    private static void registerDrinkBlockItems(boolean isNewVersion) {
        if (drinkBlockItemClass == null) {
            return;
        }
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Item item = entry.getValue();
            if (drinkBlockItemClass.isInstance(item)) {
                registerItem(item, DRINK_HYDRATION[0], DRINK_HYDRATION[1], isNewVersion);
            }
        }
    }

    private static void registerCocktailBlockItems(boolean isNewVersion) {
        if (cocktailBlockItemClass == null) {
            return;
        }
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Item item = entry.getValue();
            if (cocktailBlockItemClass.isInstance(item)) {
                registerItem(item, COCKTAIL_HYDRATION[0], COCKTAIL_HYDRATION[1], isNewVersion);
            }
        }
    }

    private static void registerItem(Item item, int hydration, int quenched, boolean isNewVersion) {
        if (isNewVersion) {
            registerNewVersion(item, hydration, quenched);
        } else {
            registerOldVersion(item, hydration, quenched);
        }
    }

    private static boolean isNewThirstVersion() {
        try {
            Class.forName("cn.mlus.thirst.api.ThirstHelper");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerNewVersion(Item item, int hydration, int quenched) {
        try {
            Class<?> thirstHelperClass = Class.forName("cn.mlus.thirst.api.ThirstHelper");
            java.lang.reflect.Field validFoodsField = thirstHelperClass.getDeclaredField("VALID_FOODS");
            validFoodsField.setAccessible(true);

            Map<Item, Number[]> validFoods = (Map<Item, Number[]>) validFoodsField.get(null);
            validFoods.put(item, new Number[]{hydration, quenched});
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerOldVersion(Item item, int hydration, int quenched) {
        try {
            Class<?> thirstHelperClass = Class.forName("dev.ghen.thirst.api.ThirstHelper");
            java.lang.reflect.Field validFoodsField = thirstHelperClass.getDeclaredField("VALID_FOODS");
            validFoodsField.setAccessible(true);

            Map<Item, Number[]> validFoods = (Map<Item, Number[]>) validFoodsField.get(null);
            validFoods.put(item, new Number[]{hydration, quenched});
        } catch (Exception ignored) {
        }
    }
}