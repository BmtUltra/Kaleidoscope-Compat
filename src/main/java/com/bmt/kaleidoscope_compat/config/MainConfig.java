package com.bmt.kaleidoscope_compat.config;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class MainConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.EnumValue<DatapackMode> DATAPACK_MODE = BUILDER
            .comment("NONE: Disable all datapacks except soup", "NONE: 不启用数据兼容",
                    "COMPAT: Extensive compatibility with other mod items", "COMPAT: 与其他模组物品与配方提供大量兼容",
                    "UNITE: Duplicate items of the unified module", "UNITE: 统一与其它模组重复的物品")
            .defineEnum("datapack.mode", DatapackMode.COMPAT);

    private static final ModConfigSpec.BooleanValue SOUP_DATAPACK_ENABLED = BUILDER
            .comment("Enable soup base material", "是否启用汤锅材质拓展")
            .define("datapack.soup_enabled", true);

    private static final ModConfigSpec.BooleanValue LUNCH_BAG_BLACKLIST_ENABLED = BUILDER
            .comment("Whether the lunch bag blacklist is enabled","是否启用嬗变饭袋物品黑名单")
            .define("transmutationLunchBag.blacklist_enabled", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LUNCH_BAG_BLACKLIST = BUILDER
            .comment("List of item IDs that cannot be put into the Transmutation Lunch Bag","黑名单物品",
                    "Format: modid:item_id (e.g., artifacts:everlasting_beef, minecraft:apple)")
            .defineList("transmutationLunchBag.blacklist",
                    Arrays.asList("artifacts:eternal_steak","kaleidoscope_nether:everlasting_flame_steak"),
                    MainConfig::validateItemName);

    private static final ModConfigSpec.BooleanValue TRANSMUTATION_LUNCH_BAG_BACK_ENABLED = BUILDER
            .comment("Whether the modified Transmutation Lunch Bag behavior is enabled","是否启用嬗变饭袋回调",
                    "When enabled, the lunch bag will only consume the first food item but apply all effects from all items in the bag",
                    "When disabled, the original Kaleidoscope Cookery behavior will be used")
            .define("transmutationLunchBag.back_behavior_enabled", false);

    private static final ModConfigSpec.BooleanValue MILLSTONE_STACKING_ENABLED = BUILDER
            .comment("Whether millstone item stacking is enabled", "是否启用石磨输入限制突破",
                    "When enabled, you can add more items of the same type while the millstone is working",
                    "当启用时，部分功能可以在石磨工作时添加同类型的物品进行堆叠")
            .define("millstone.stacking_enabled", true);

    private static final ModConfigSpec.BooleanValue COOKING_POT_RECIPES_DISABLED = BUILDER
            .comment("Whether all FarmersDelight cooking pot recipes are disabled","是否禁用农夫乐事厨锅配方",
                    "When enabled, all cooking pot recipes will not work")
            .define("farmersdelight.cooking_pot_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue COOKING_POT_GUI_DISABLED = BUILDER
            .comment("Whether the FarmersDelight cooking pot GUI is disabled", "是否禁用农夫乐事厨锅",
                    "When enabled, the cooking pot GUI will not open")
            .define("farmersdelight.cooking_pot_gui_disabled", false);

    private static final ModConfigSpec.BooleanValue CUTTING_BOARD_RECIPES_DISABLED = BUILDER
            .comment("Whether all FarmersDelight cutting board recipes are disabled","是否禁用农夫乐事砧板配方",
                    "When enabled, all cutting board recipes will not work")
            .define("farmersdelight.cutting_board_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue STEAMING_RECIPES_DISABLED = BUILDER
            .comment("Whether all Youkai's Homecoming steaming recipes are disabled","是否禁用妖怪归家蒸笼配方",
                    "When enabled, all steaming recipes will not work")
            .define("youkaisfeasts.steaming_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue RICH_SOIL_HOE_ENABLED = BUILDER
            .comment("Whether rich soil hoe tilling is enabled", "是否启用水中锄耕农夫乐事沃土功能",
                    "When enabled, right-clicking rich soil with water above will turn it into rich soil farmland")
            .define("farmersdelight.hoe_enabled", true);

    private static final ModConfigSpec.BooleanValue FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED = BUILDER
            .comment("Whether all Farm and Charm cooking pot recipes are disabled","是否禁用沉浸农艺厨锅配方",
                    "When enabled, all Farm and Charm cooking pot recipes will not work")
            .define("farm_and_charm.cooking_pot_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue VINERY_BARREL_RECIPES_DISABLED = BUILDER
            .comment("Whether all Vinery fermentation barrel recipes are disabled","是否禁用葡园酒香酿造桶配方",
                    "When enabled, all Vinery fermentation barrel recipes will not work")
            .define("vinery.barrel_recipes_disabled", false);

    private static final ModConfigSpec.BooleanValue SCARECROW_REPEL_PHANTOMS = BUILDER
            .comment("Whether scarecrows can repel phantoms", "稻草人是否可以驱散幻翼",
                    "When enabled, phantoms will avoid areas near scarecrows with heads")
            .define("scarecrow.repel_phantoms", true);

    private static final ModConfigSpec.BooleanValue NOURISHMENT_EFFECT_BLOCK_ENABLED = BUILDER
            .comment("Whether Nourishment effect is blocked when player has Satiated Shield","是否启用滋养和饱腹代偿修复",
                    "When enabled, FarmersDelight's Nourishment effect will not work if player has Satiated Shield effect")
            .define("effect.block_enabled", true);

    private static final ModConfigSpec.BooleanValue PROJECTILE_DODGE_TELEPORT_ENABLED = BUILDER
            .comment("Whether projectile dodge teleport is enabled", "是否启用弹射闪避的传送",
                    "When enabled, players with Projectile Dodge effect will teleport when hit by projectiles",
                    "When disabled, projectiles will be cancelled without teleportation")
            .define("effect.teleport_enabled", false);

    private static final ModConfigSpec.IntValue PROJECTILE_DODGE_DURATION_COST = BUILDER
            .comment("Duration cost per dodge (in ticks)", "每次闪避消耗的持续时间（tick）",
                    "Default: 200 ticks (10 seconds)")
            .defineInRange("effect.duration_cost", 200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> VITALITY_BLACKLIST = BUILDER
            .comment("List of entity IDs that cannot be affected by Vitality effect", "生机效果黑名单实体ID",
                    "Format: modid:entity_id (e.g., minecraft:zombie, minecraft:villager)")
            .defineList("effect.blacklist", Collections.emptyList(), MainConfig::validateEntityName);

    private static final ModConfigSpec.BooleanValue LITTLE_MAID_COMPAT_ENABLED = BUILDER
            .comment("Whether Little Maid mod compatibility is enabled", "是否启用女仆任务拓展模块",
                    "When disabled, all Little Maid compatibility features will not be loaded")
            .define("little_maid.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue LITTLE_MAID_CHOPPING_BOARD_ENABLED = BUILDER
            .comment("Whether Little Maid chopping board task is enabled", "是否启用女仆菜板任务",
                    "When enabled, maids can cut ingredients on the chopping board")
            .define("little_maid.chopping_board_enabled", true);

    private static final ModConfigSpec.BooleanValue LITTLE_MAID_MILLSTONE_ENABLED = BUILDER
            .comment("Whether Little Maid millstone task is enabled", "是否启用女仆石磨任务",
                    "When enabled, maids can grind ingredients on the millstone")
            .define("little_maid.millstone_enabled", true);

    private static final ModConfigSpec.BooleanValue LITTLE_MAID_PRESSING_TUB_ENABLED = BUILDER
            .comment("Whether Little Maid pressing tub task is enabled", "是否启用女仆果盘任务",
                    "When enabled, maids can press ingredients in the pressing tub")
            .define("little_maid.pressing_tub_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_COMPAT_ENABLED = BUILDER
            .comment("Whether Spectrum mod compatibility is enabled", "是否启用光谱世界兼容模块",
                    "When disabled, all Spectrum compatibility features will not be loaded")
            .define("spectrum.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_POT_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum pot item handler is enabled", "是否启用炒锅节点传输",
                    "When enabled, Spectrum mod can interact with the pot via item handler")
            .define("spectrum.pot_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_CHOPPING_BOARD_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum chopping board item handler is enabled", "是否启用切菜板节点传输",
                    "When enabled, Spectrum mod can interact with the chopping board via item handler")
            .define("spectrum.chopping_board_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_MILLSTONE_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum millstone item handler is enabled", "是否启用石磨节点传输",
                    "When enabled, Spectrum mod can interact with the millstone via item handler")
            .define("spectrum.millstone_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_SHAWARMA_SPIT_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum shawarma spit item handler is enabled", "是否启用旋风烤肉塔节点传输",
                    "When enabled, Spectrum mod can interact with the shawarma spit via item handler")
            .define("spectrum.shawarma_spit_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_STEAMER_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum steamer item handler is enabled", "是否启用蒸笼节点传输",
                    "When enabled, Spectrum mod can interact with the steamer via item handler")
            .define("spectrum.steamer_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_TEAPOT_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum teapot item handler is enabled", "是否启用茶壶节点传输",
                    "When enabled, Spectrum mod can interact with the teapot via item handler")
            .define("spectrum.teapot_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_TRASH_CAN_ITEM_HANDLER_ENABLED = BUILDER
            .comment("Whether Spectrum trash can item handler is enabled", "是否启用垃圾桶节点传输",
                    "When enabled, Spectrum mod can interact with the trash can via item handler")
            .define("spectrum.trash_can_item_handler_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_BLOCK_PLACER_COMPAT_ENABLED = BUILDER
            .comment("Whether Spectrum block placer stockpot lid compatibility is enabled", "是否启用方块放置器上盖",
                    "When enabled, block placer can place stockpot lids on stockpots")
            .define("spectrum.block_placer_compat_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_BLOCK_BREAKER_COMPAT_ENABLED = BUILDER
            .comment("Whether Spectrum block breaker stockpot lid compatibility is enabled", "是否启用方块破坏器取盖",
                    "When enabled, block breaker will only break the lid of a stockpot instead of the whole pot")
            .define("spectrum.block_breaker_compat_enabled", true);

    private static final ModConfigSpec.BooleanValue SPECTRUM_PASTEL_NODE_COMPAT_ENABLED = BUILDER
            .comment("Whether Spectrum pastel node pot oil compatibility is enabled", "是否启用节点炒锅加油",
                    "When enabled, pastel nodes can add oil to pots")
            .define("spectrum.pastel_node_compat_enabled", true);


    private static final ModConfigSpec.BooleanValue CREATE_COMPAT_ENABLED = BUILDER
            .comment("Whether Create mod compatibility is enabled", "是否启用机械动力兼容模块",
                    "When disabled, all Create compatibility features will not be loaded")
            .define("create.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_POT_ENABLED = BUILDER
            .comment("Whether Create mechanical arm pot compatibility is enabled", "是否启用机械动力动力臂炒锅兼容")
            .define("create.arm_pot_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_STOCKPOT_ENABLED = BUILDER
            .comment("Whether Create mechanical arm stockpot compatibility is enabled", "是否启用机械动力动力臂汤锅兼容")
            .define("create.arm_stockpot_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_STEAMER_ENABLED = BUILDER
            .comment("Whether Create mechanical arm steamer compatibility is enabled", "是否启用机械动力动力臂蒸笼兼容")
            .define("create.arm_steamer_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_MILLSTONE_ENABLED = BUILDER
            .comment("Whether Create mechanical arm millstone compatibility is enabled", "是否启用机械动力动力臂磨盘兼容")
            .define("create.arm_millstone_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_SHAWARMA_SPIT_ENABLED = BUILDER
            .comment("Whether Create mechanical arm shawarma spit compatibility is enabled", "是否启用机械动力动力臂烤肉塔兼容")
            .define("create.arm_shawarma_spit_enabled", true);

    private static final ModConfigSpec.BooleanValue CREATE_ARM_TEAPOT_ENABLED = BUILDER
            .comment("Whether Create mechanical arm teapot compatibility is enabled", "是否启用机械动力动力臂茶壶兼容")
            .define("create.arm_teapot_enabled", true);

    private static final ModConfigSpec.BooleanValue APPLESKIN_COMPAT_ENABLED = BUILDER
            .comment("Whether AppleSkin mod compatibility is enabled", "是否启用苹果皮和嬗变饭袋的兼容")
            .define("transmutationLunchBag.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue FARM_AND_CHARM_COMPAT_ENABLED = BUILDER
            .comment("Whether Farm and Charm mod compatibility is enabled", "是否启用沉浸农艺锅配方兼容")
            .define("farm_and_charm.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue JEI_COMPAT_ENABLED = BUILDER
            .comment("Whether JEI compatibility is enabled", "是否启用旋风烤肉塔JEI")
            .define("jei.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue THIRST_COMPAT_ENABLED = BUILDER
            .comment("Whether Thirst mod compatibility is enabled", "是否启用口渴兼容")
            .define("thirst.compat_enabled", true);

    private static final ModConfigSpec.BooleanValue QUARK_SICKLE_HARVEST_FIX_ENABLED = BUILDER
            .comment("Whether Quark sickle harvest fix is enabled", "是否启用夸克兼容",
                    "When enabled, sickle items will not trigger Quark's automatic harvest")
            .define("quark.sickle_harvest_fix_enabled", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static DatapackMode datapackMode = DatapackMode.COMPAT;
    public static boolean soupDatapackEnabled = true;
    public static boolean lunchBagBlacklistEnabled = true;
    public static Set<ResourceLocation> lunchBagBlacklist = new HashSet<>();
    public static boolean transmutationLunchBagBackEnabled = true;
    public static boolean cookingPotRecipesDisabled = false;
    public static boolean cuttingBoardRecipesDisabled = false;
    public static boolean richSoilHoeEnabled = true;
    public static boolean farmAndCharmCookingPotRecipesDisabled = false;
    public static boolean vineryBarrelRecipesDisabled = false;
    public static boolean scarecrowRepelPhantoms = true;
    public static boolean nourishmentEffectBlockEnabled = true;
    public static boolean projectileDodgeTeleportEnabled = false;
    public static int projectileDodgeDurationCost = 200;
    public static Set<ResourceLocation> vitalityBlacklist = new HashSet<>();
    public static boolean createCompatEnabled = true;
    public static boolean createArmPotEnabled = true;
    public static boolean createArmStockpotEnabled = true;
    public static boolean createArmSteamerEnabled = true;
    public static boolean createArmMillstoneEnabled = true;
    public static boolean createArmShawarmaSpitEnabled = true;
    public static boolean createArmTeapotEnabled = true;
    public static boolean appleskinCompatEnabled = true;
    public static boolean farmAndCharmCompatEnabled = true;
    public static boolean jeiCompatEnabled = true;
    public static boolean thirstCompatEnabled = true;
    public static boolean quarkSickleHarvestFixEnabled = true;
    public static boolean steamingRecipesDisabled = false;
    public static boolean cookingPotGuiDisabled = false;
    public static boolean millstoneStackingEnabled = true;
    public static boolean spectrumCompatEnabled = true;
    public static boolean spectrumMillstoneItemHandlerEnabled = true;
    public static boolean spectrumBlockPlacerCompatEnabled = true;
    public static boolean spectrumBlockBreakerCompatEnabled = true;
    public static boolean spectrumPastelNodeCompatEnabled = true;
    public static boolean spectrumSteamerItemHandlerEnabled = true;
    public static boolean spectrumTrashCanItemHandlerEnabled = true;
    public static boolean spectrumChoppingBoardItemHandlerEnabled = true;
    public static boolean spectrumTeapotItemHandlerEnabled = true;
    public static boolean spectrumShawarmaSpitItemHandlerEnabled = true;
    public static boolean spectrumPotItemHandlerEnabled = true;
    public static boolean littleMaidCompatEnabled = true;
    public static boolean littleMaidChoppingBoardEnabled = true;
    public static boolean littleMaidMillstoneEnabled = true;
    public static boolean littleMaidPressingTubEnabled = true;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        datapackMode = DATAPACK_MODE.get();
        soupDatapackEnabled = SOUP_DATAPACK_ENABLED.get();
        lunchBagBlacklistEnabled = LUNCH_BAG_BLACKLIST_ENABLED.get();
        transmutationLunchBagBackEnabled = TRANSMUTATION_LUNCH_BAG_BACK_ENABLED.get();
        lunchBagBlacklist.clear();
        for (String itemStr : LUNCH_BAG_BLACKLIST.get()) {
            ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
            if (itemId != null) {
                lunchBagBlacklist.add(itemId);
            }
        }
        cookingPotRecipesDisabled = COOKING_POT_RECIPES_DISABLED.get();
        cuttingBoardRecipesDisabled = CUTTING_BOARD_RECIPES_DISABLED.get();
        richSoilHoeEnabled = RICH_SOIL_HOE_ENABLED.get();
        farmAndCharmCookingPotRecipesDisabled = FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED.get();
        vineryBarrelRecipesDisabled = VINERY_BARREL_RECIPES_DISABLED.get();
        scarecrowRepelPhantoms = SCARECROW_REPEL_PHANTOMS.get();
        nourishmentEffectBlockEnabled = NOURISHMENT_EFFECT_BLOCK_ENABLED.get();
        projectileDodgeTeleportEnabled = PROJECTILE_DODGE_TELEPORT_ENABLED.get();
        projectileDodgeDurationCost = PROJECTILE_DODGE_DURATION_COST.get();
        vitalityBlacklist.clear();
        for (String entityStr : VITALITY_BLACKLIST.get()) {
            ResourceLocation entityId = ResourceLocation.tryParse(entityStr);
            if (entityId != null) {
                vitalityBlacklist.add(entityId);
            }
        }
        createCompatEnabled = CREATE_COMPAT_ENABLED.get();
        createArmPotEnabled = CREATE_ARM_POT_ENABLED.get();
        createArmStockpotEnabled = CREATE_ARM_STOCKPOT_ENABLED.get();
        createArmSteamerEnabled = CREATE_ARM_STEAMER_ENABLED.get();
        createArmMillstoneEnabled = CREATE_ARM_MILLSTONE_ENABLED.get();
        createArmShawarmaSpitEnabled = CREATE_ARM_SHAWARMA_SPIT_ENABLED.get();
        createArmTeapotEnabled = CREATE_ARM_TEAPOT_ENABLED.get();
        appleskinCompatEnabled = APPLESKIN_COMPAT_ENABLED.get();
        farmAndCharmCompatEnabled = FARM_AND_CHARM_COMPAT_ENABLED.get();
        jeiCompatEnabled = JEI_COMPAT_ENABLED.get();
        thirstCompatEnabled = THIRST_COMPAT_ENABLED.get();
        quarkSickleHarvestFixEnabled = QUARK_SICKLE_HARVEST_FIX_ENABLED.get();
        steamingRecipesDisabled = STEAMING_RECIPES_DISABLED.get();
        cookingPotGuiDisabled = COOKING_POT_GUI_DISABLED.get();
        millstoneStackingEnabled = MILLSTONE_STACKING_ENABLED.get();
        spectrumCompatEnabled = SPECTRUM_COMPAT_ENABLED.get();
        spectrumMillstoneItemHandlerEnabled = SPECTRUM_MILLSTONE_ITEM_HANDLER_ENABLED.get();
        spectrumBlockPlacerCompatEnabled = SPECTRUM_BLOCK_PLACER_COMPAT_ENABLED.get();
        spectrumBlockBreakerCompatEnabled = SPECTRUM_BLOCK_BREAKER_COMPAT_ENABLED.get();
        spectrumPastelNodeCompatEnabled = SPECTRUM_PASTEL_NODE_COMPAT_ENABLED.get();
        spectrumSteamerItemHandlerEnabled = SPECTRUM_STEAMER_ITEM_HANDLER_ENABLED.get();
        spectrumTrashCanItemHandlerEnabled = SPECTRUM_TRASH_CAN_ITEM_HANDLER_ENABLED.get();
        spectrumChoppingBoardItemHandlerEnabled = SPECTRUM_CHOPPING_BOARD_ITEM_HANDLER_ENABLED.get();
        spectrumTeapotItemHandlerEnabled = SPECTRUM_TEAPOT_ITEM_HANDLER_ENABLED.get();
        spectrumShawarmaSpitItemHandlerEnabled = SPECTRUM_SHAWARMA_SPIT_ITEM_HANDLER_ENABLED.get();
        spectrumPotItemHandlerEnabled = SPECTRUM_POT_ITEM_HANDLER_ENABLED.get();
        littleMaidCompatEnabled = LITTLE_MAID_COMPAT_ENABLED.get();
        littleMaidChoppingBoardEnabled = LITTLE_MAID_CHOPPING_BOARD_ENABLED.get();
        littleMaidMillstoneEnabled = LITTLE_MAID_MILLSTONE_ENABLED.get();
        littleMaidPressingTubEnabled = LITTLE_MAID_PRESSING_TUB_ENABLED.get();
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

    private static boolean validateEntityName(final Object obj) {
        if (!(obj instanceof String entityStr)) {
            return false;
        }
        return ResourceLocation.tryParse(entityStr) != null;
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