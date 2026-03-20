package com.bmt.kaleidoscope_compat.config;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.DatapackMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class KCConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.EnumValue<DatapackMode> DATAPACK_MODE = BUILDER
            .comment("COMPAT: Extensive compatibility with other mod items",
                    "UNITE: Duplicate items of the unified module")
            .defineEnum("datapack.mode", DatapackMode.COMPAT);

    private static final ModConfigSpec.BooleanValue SOUP_DATAPACK_ENABLED = BUILDER
            .comment("Enable soup base material")
            .define("datapack.soup_enabled", true);

    private static final ModConfigSpec.BooleanValue LUNCH_BAG_BLACKLIST_ENABLED = BUILDER
            .comment("Whether the lunch bag blacklist is enabled")
            .define("lunchBag.blacklist_enabled", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LUNCH_BAG_BLACKLIST = BUILDER
            .comment("List of item IDs that cannot be put into the Transmutation Lunch Bag",
                    "Format: modid:item_id (e.g., artifacts:everlasting_beef, minecraft:apple)")
            .defineList("lunchBag.blacklist",
                    Arrays.asList("artifacts:eternal_steak","kaleidoscope_nether:everlasting_flame_steak"),
                    KCConfig::validateItemName);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static DatapackMode datapackMode = DatapackMode.COMPAT;
    public static boolean soupDatapackEnabled = true;
    public static boolean lunchBagBlacklistEnabled = true;
    public static Set<ResourceLocation> lunchBagBlacklist = new HashSet<>();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        datapackMode = DATAPACK_MODE.get();
        soupDatapackEnabled = SOUP_DATAPACK_ENABLED.get();
        lunchBagBlacklistEnabled = LUNCH_BAG_BLACKLIST_ENABLED.get();

        lunchBagBlacklist.clear();
        for (String itemStr : LUNCH_BAG_BLACKLIST.get()) {
            ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
            if (itemId != null) {
                lunchBagBlacklist.add(itemId);
            }
        }
    }

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemStr)) {
            return false;
        }
        return ResourceLocation.tryParse(itemStr) != null;
    }

    public static boolean isItemBlacklisted(Item item) {
        if (!lunchBagBlacklistEnabled) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return lunchBagBlacklist.contains(itemId);
    }

    @Nullable
    public static Item getItem(String itemName) {
        ResourceLocation itemId = ResourceLocation.tryParse(itemName);
        if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
            return BuiltInRegistries.ITEM.get(itemId);
        }
        return null;
    }
}