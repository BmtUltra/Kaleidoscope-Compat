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
            .define("lunchBlacklist.blacklist_enabled", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LUNCH_BAG_BLACKLIST = BUILDER
            .comment("List of item IDs that cannot be put into the Transmutation Lunch Bag",
                    "Format: modid:item_id (e.g., artifacts:everlasting_beef, minecraft:apple)")
            .defineList("lunchBlacklist.blacklist",
                    Arrays.asList("artifacts:eternal_steak","kaleidoscope_nether:everlasting_flame_steak"),
                    KCConfig::validateItemName);

    private static final ModConfigSpec.BooleanValue SATIATED_SHIELD_WEAKEN_ENABLED = BUILDER
            .comment("Whether the weakened satiated shield effect is enabled",
                    "When enabled, satiated shield will be weakened according to config settings")
            .define("satiatedShield.weaken_enabled", false);

    private static final ModConfigSpec.DoubleValue SATIATED_SHIELD_DAMAGE_REDUCTION_RATIO = BUILDER
            .comment("Damage reduction ratio for weakened satiated shield effect",
                    "Value between 0.0 (no reduction) and 1.0 (100% reduction)",
                    "Default: 0.5 (50% damage reduction)")
            .defineInRange("satiatedShield.damage_reduction_ratio", 0.5, 0.0, 1.0);

    private static final ModConfigSpec.BooleanValue NOURISHMENT_EFFECT_BLOCK_ENABLED = BUILDER
            .comment("Whether Nourishment effect is blocked when player has Satiated Shield",
                    "When enabled, FarmersDelight's Nourishment effect will not work if player has Satiated Shield effect")
            .define("nourishmentEffect.block_enabled", true);

    private static final ModConfigSpec.BooleanValue TRANSMUTATION_LUNCH_BAG_BACK_ENABLED = BUILDER
            .comment("Whether the modified Transmutation Lunch Bag behavior is enabled",
                    "When enabled, the lunch bag will only consume the first food item but apply all effects from all items in the bag",
                    "When disabled, the original Kaleidoscope Cookery behavior will be used")
            .define("transmutationLunchBag.back_behavior_enabled", false);

    private static final ModConfigSpec.BooleanValue COOKING_POT_RECIPES_DISABLED = BUILDER
            .comment("Whether all FarmersDelight cooking pot recipes are disabled",
                    "When enabled, all cooking pot recipes will not work")
            .define("farmersdelight.cooking_pot_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue CUTTING_BOARD_RECIPES_DISABLED = BUILDER
            .comment("Whether all FarmersDelight cutting board recipes are disabled",
                    "When enabled, all cutting board recipes will not work")
            .define("farmersdelight.cutting_board_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED = BUILDER
            .comment("Whether all Farm and Charm cooking pot recipes are disabled",
                    "When enabled, all Farm and Charm cooking pot recipes will not work")
            .define("farm_and_charm.cooking_pot_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue VINERY_BARREL_RECIPES_DISABLED = BUILDER
            .comment("Whether all Vinery fermentation barrel recipes are disabled",
                    "When enabled, all Vinery fermentation barrel recipes will not work")
            .define("vinery.barrel_recipes_disabled", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static DatapackMode datapackMode = DatapackMode.COMPAT;
    public static boolean soupDatapackEnabled = true;
    public static boolean lunchBagBlacklistEnabled = true;
    public static Set<ResourceLocation> lunchBagBlacklist = new HashSet<>();

    public static boolean satiatedShieldWeakenEnabled = true;
    public static double satiatedShieldDamageReductionRatio = 0.5;
    public static boolean nourishmentEffectBlockEnabled = true;

    public static boolean transmutationLunchBagBackEnabled = true;
    public static boolean cookingPotRecipesDisabled = false;
    public static boolean cuttingBoardRecipesDisabled = false;

    public static boolean farmAndCharmCookingPotRecipesDisabled = false;
    public static boolean vineryBarrelRecipesDisabled = false;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        datapackMode = DATAPACK_MODE.get();
        soupDatapackEnabled = SOUP_DATAPACK_ENABLED.get();
        lunchBagBlacklistEnabled = LUNCH_BAG_BLACKLIST_ENABLED.get();
        satiatedShieldWeakenEnabled = SATIATED_SHIELD_WEAKEN_ENABLED.get();
        satiatedShieldDamageReductionRatio = SATIATED_SHIELD_DAMAGE_REDUCTION_RATIO.get();
        nourishmentEffectBlockEnabled = NOURISHMENT_EFFECT_BLOCK_ENABLED.get();
        transmutationLunchBagBackEnabled = TRANSMUTATION_LUNCH_BAG_BACK_ENABLED.get();
        cookingPotRecipesDisabled = COOKING_POT_RECIPES_DISABLED.get();
        cuttingBoardRecipesDisabled = CUTTING_BOARD_RECIPES_DISABLED.get();
        farmAndCharmCookingPotRecipesDisabled = FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED.get();
        vineryBarrelRecipesDisabled = VINERY_BARREL_RECIPES_DISABLED.get();

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