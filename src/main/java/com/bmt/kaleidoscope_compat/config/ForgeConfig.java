package com.bmt.kaleidoscope_compat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = "kaleidoscope_compat", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeConfig {
    public static final ForgeConfigSpec.ConfigValue<String> DATAPACK_MODE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SOUP_DATAPACK_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> KITCHEN_FUZZY_RECIPES_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> KITCHEN_BLOCK_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LUNCH_BAG_BACK_BEHAVIOR_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LUNCH_BAG_APPLESKIN_COMPAT_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SCARECROW_REPEL_PHANTOMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARMERSDELIGHT_COOKING_POT_RECIPES_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARMERSDELIGHT_COOKING_POT_GUI_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARMERSDELIGHT_CUTTING_BOARD_RECIPES_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARMERSDELIGHT_RICH_SOIL_HOE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> YOUKAISFEASTS_STEAMING_RECIPES_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FARM_AND_CHARM_COMPAT_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VINERY_BARREL_RECIPES_DISABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LITTLE_MAID_CHOPPING_BOARD_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LITTLE_MAID_MILLSTONE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LITTLE_MAID_PRESSING_TUB_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OTHER_JEI_COMPAT_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OTHER_THIRST_COMPAT_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OTHER_QUARK_SICKLE_HARVEST_FIX_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OTHER_SUPPRESS_TAG_LOAD_ERRORS;
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Main configuration for Kaleidoscope Compat").push("general");

        DATAPACK_MODE = builder
                .comment("NONE: Disable all datapacks except soup\nCOMPAT: Extensive compatibility with other mod items\nUNITE: Duplicate items of the unified module")
                .define("datapack_mode", "COMPAT");

        SOUP_DATAPACK_ENABLED = builder
                .comment("Enable soup base material")
                .define("soup_datapack_enabled", true);

        builder.pop();

        builder.comment("Kitchen mod compatibility settings").push("kitchen");

        KITCHEN_FUZZY_RECIPES_ENABLED = builder
                .comment("Enable fuzzy recipes (FlexPotRecipe and FlexStockpotRecipe)")
                .define("fuzzy_recipes_enabled", true);

        KITCHEN_BLOCK_ENABLED = builder
                .comment("Whether Nourishment effect is blocked when player has Satiated Shield")
                .define("block_enabled", true);

        builder.push("lunch_bag");
        LUNCH_BAG_BACK_BEHAVIOR_ENABLED = builder
                .comment("Whether the modified Transmutation Lunch Bag behavior is enabled")
                .define("back_behavior_enabled", false);
        LUNCH_BAG_APPLESKIN_COMPAT_ENABLED = builder
                .comment("Whether AppleSkin mod compatibility is enabled")
                .define("appleskin_compat_enabled", true);
        builder.pop();

        builder.push("scarecrow");
        SCARECROW_REPEL_PHANTOMS = builder
                .comment("Whether scarecrows can repel phantoms")
                .define("repel_phantoms", true);
        builder.pop();

        builder.pop();

        builder.comment("FarmersDelight mod compatibility settings").push("farmersdelight");

        FARMERSDELIGHT_COOKING_POT_RECIPES_DISABLED = builder
                .comment("Whether all FarmersDelight cooking pot recipes are disabled")
                .define("cooking_pot_recipes_disabled", false);

        FARMERSDELIGHT_COOKING_POT_GUI_DISABLED = builder
                .comment("Whether the FarmersDelight cooking pot GUI is disabled")
                .define("cooking_pot_gui_disabled", false);

        FARMERSDELIGHT_CUTTING_BOARD_RECIPES_DISABLED = builder
                .comment("Whether all FarmersDelight cutting board recipes are disabled")
                .define("cutting_board_recipes_disabled", false);

        FARMERSDELIGHT_RICH_SOIL_HOE_ENABLED = builder
                .comment("Whether rich soil hoe tilling is enabled")
                .define("rich_soil_hoe_enabled", true);

        builder.pop();

        builder.comment("Youkai's Feasts mod compatibility settings").push("youkaisfeasts");

        YOUKAISFEASTS_STEAMING_RECIPES_DISABLED = builder
                .comment("Whether all Youkai's Homecoming steaming recipes are disabled")
                .define("steaming_recipes_disabled", false);

        builder.pop();

        builder.comment("Farm and Charm mod compatibility settings").push("farm_and_charm");

        FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED = builder
                .comment("Whether all Farm and Charm cooking pot recipes are disabled")
                .define("cooking_pot_recipes_disabled", false);

        FARM_AND_CHARM_COMPAT_ENABLED = builder
                .comment("Whether Farm and Charm mod compatibility is enabled")
                .define("compat_enabled", true);

        builder.pop();

        builder.comment("Vinery mod compatibility settings").push("vinery");

        VINERY_BARREL_RECIPES_DISABLED = builder
                .comment("Whether all Vinery fermentation barrel recipes are disabled")
                .define("barrel_recipes_disabled", false);

        builder.pop();

        builder.comment("Little Maid mod compatibility settings").push("little_maid");

        LITTLE_MAID_CHOPPING_BOARD_ENABLED = builder
                .comment("Whether Little Maid chopping board task is enabled")
                .define("chopping_board_enabled", true);

        LITTLE_MAID_MILLSTONE_ENABLED = builder
                .comment("Whether Little Maid millstone task is enabled")
                .define("millstone_enabled", true);

        LITTLE_MAID_PRESSING_TUB_ENABLED = builder
                .comment("Whether Little Maid pressing tub task is enabled")
                .define("pressing_tub_enabled", true);

        builder.pop();

        builder.comment("Other compatibility settings").push("other");

        OTHER_JEI_COMPAT_ENABLED = builder
                .comment("Whether JEI compatibility is enabled")
                .define("jei_compat_enabled", true);

        OTHER_THIRST_COMPAT_ENABLED = builder
                .comment("Whether Thirst mod compatibility is enabled")
                .define("thirst_compat_enabled", true);

        OTHER_QUARK_SICKLE_HARVEST_FIX_ENABLED = builder
                .comment("Whether Quark sickle harvest fix is enabled")
                .define("quark_sickle_harvest_fix_enabled", true);

        OTHER_SUPPRESS_TAG_LOAD_ERRORS = builder
                .comment("Whether to suppress tag file loading error logs")
                .define("suppress_tag_load_errors", false);

        builder.pop();

        SPEC = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
    }
}