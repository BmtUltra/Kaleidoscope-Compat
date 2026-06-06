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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class MainConfig {
    public static final MainConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.EnumValue<DatapackMode> datapackMode;
    public final ModConfigSpec.BooleanValue soupDatapackEnabled;

    public final ModConfigSpec.BooleanValue millstoneStackingEnabled;

    public final ModConfigSpec.BooleanValue lunchBagBlacklistEnabled;
    public final ModConfigSpec.ConfigValue<List<? extends String>> lunchBagBlacklist;
    public final ModConfigSpec.BooleanValue transmutationLunchBagBackEnabled;
    public final ModConfigSpec.BooleanValue appleskinCompatEnabled;

    public final ModConfigSpec.BooleanValue scarecrowRepelPhantoms;

    public final ModConfigSpec.BooleanValue nourishmentEffectBlockEnabled;
    public final ModConfigSpec.BooleanValue projectileDodgeTeleportEnabled;
    public final ModConfigSpec.ConfigValue<Integer> projectileDodgeDurationCost;
    public final ModConfigSpec.ConfigValue<List<? extends String>> vitalityBlacklist;

    public final ModConfigSpec.BooleanValue cookingPotRecipesDisabled;
    public final ModConfigSpec.BooleanValue cookingPotGuiDisabled;
    public final ModConfigSpec.BooleanValue cuttingBoardRecipesDisabled;
    public final ModConfigSpec.BooleanValue richSoilHoeEnabled;

    public final ModConfigSpec.BooleanValue steamingRecipesDisabled;

    public final ModConfigSpec.BooleanValue farmAndCharmCookingPotRecipesDisabled;

    public final ModConfigSpec.BooleanValue vineryBarrelRecipesDisabled;

    public final ModConfigSpec.BooleanValue littleMaidCompatEnabled;
    public final ModConfigSpec.BooleanValue littleMaidChoppingBoardEnabled;
    public final ModConfigSpec.BooleanValue littleMaidMillstoneEnabled;
    public final ModConfigSpec.BooleanValue littleMaidPressingTubEnabled;

    public final ModConfigSpec.BooleanValue spectrumCompatEnabled;
    public final ModConfigSpec.BooleanValue spectrumPotItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumChoppingBoardItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumMillstoneItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumShawarmaSpitItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumSteamerItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumTeapotItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumTrashCanItemHandlerEnabled;
    public final ModConfigSpec.BooleanValue spectrumPastelNodeCompatEnabled;

    public final ModConfigSpec.BooleanValue createCompatEnabled;
    public final ModConfigSpec.BooleanValue createArmPotEnabled;
    public final ModConfigSpec.BooleanValue createArmStockpotEnabled;
    public final ModConfigSpec.BooleanValue createArmSteamerEnabled;
    public final ModConfigSpec.BooleanValue createArmMillstoneEnabled;
    public final ModConfigSpec.BooleanValue createArmShawarmaSpitEnabled;
    public final ModConfigSpec.BooleanValue createArmTeapotEnabled;

    public final ModConfigSpec.BooleanValue farmAndCharmCompatEnabled;
    public final ModConfigSpec.BooleanValue jeiCompatEnabled;
    public final ModConfigSpec.BooleanValue thirstCompatEnabled;
    public final ModConfigSpec.BooleanValue quarkSickleHarvestFixEnabled;

    private MainConfig(ModConfigSpec.Builder builder) {
        builder.push("datapack").comment("数据包设置");
        this.datapackMode = builder
                .comment("NONE: Disable all datapacks except soup", "NONE: 不启用数据兼容",
                        "COMPAT: Extensive compatibility with other mod items", "COMPAT: 与其他模组物品与配方提供大量兼容",
                        "UNITE: Duplicate items of the unified module", "UNITE: 统一与其它模组重复的物品")
                .defineEnum("mode", DatapackMode.COMPAT);
        this.soupDatapackEnabled = builder
                .comment("Enable soup base material", "是否启用汤锅材质拓展")
                .define("soup_enabled", true);
        builder.pop();


        builder.push("kitchen");
        builder.comment("森罗物语：厨房");

        builder.push("millstone").comment("石磨");
        this.millstoneStackingEnabled = builder
                .comment("Whether millstone item stacking is enabled", "是否启用石磨输入限制突破",
                        "When enabled, you can add more items of the same type while the millstone is working",
                        "当启用时，部分功能可以在石磨工作时添加同类型的物品进行堆叠")
                .define("stacking_enabled", true);
        builder.pop();

        builder.push("lunch_bag").comment("嬗变饭袋");
        this.lunchBagBlacklistEnabled = builder
                .comment("Whether the lunch bag blacklist is enabled", "是否启用嬗变饭袋物品黑名单")
                .define("blacklist_enabled", true);
        this.lunchBagBlacklist = builder
                .comment("List of item IDs that cannot be put into the Transmutation Lunch Bag", "黑名单物品",
                        "Format: modid:item_id (e.g., artifacts:everlasting_beef, minecraft:apple)")
                .defineList("blacklist",
                        Arrays.asList("artifacts:eternal_steak", "kaleidoscope_nether:everlasting_flame_steak"),
                        MainConfig::validateItemName);
        this.transmutationLunchBagBackEnabled = builder
                .comment("Whether the modified Transmutation Lunch Bag behavior is enabled", "是否启用嬗变饭袋回调",
                        "When enabled, the lunch bag will only consume the first food item but apply all effects from all items in the bag",
                        "When disabled, the original Kaleidoscope Cookery behavior will be used")
                .define("back_behavior_enabled", false);
        this.appleskinCompatEnabled = builder
                .comment("Whether AppleSkin mod compatibility is enabled", "是否启用苹果皮和嬗变饭袋的兼容")
                .define("appleskin_compat_enabled", true);
        builder.pop();

        builder.push("scarecrow").comment("稻草人");
        this.scarecrowRepelPhantoms = builder
                .comment("Whether scarecrows can repel phantoms", "稻草人是否可以驱散幻翼",
                        "When enabled, phantoms will avoid areas near scarecrows with heads")
                .define("repel_phantoms", true);
        builder.pop();

        builder.push("effect");
        builder.comment("效果设置");

        this.nourishmentEffectBlockEnabled = builder
                .comment("Whether Nourishment effect is blocked when player has Satiated Shield", "是否启用滋养和饱腹代偿修复",
                        "When enabled, FarmersDelight's Nourishment effect will not work if player has Satiated Shield effect")
                .define("block_enabled", true);

        builder.push("projectile_dodge").comment("弹射闪避");
        this.projectileDodgeTeleportEnabled = builder
                .comment("Whether projectile dodge teleport is enabled", "是否启用弹射闪避的传送",
                        "When enabled, players with Projectile Dodge effect will teleport when hit by projectiles",
                        "When disabled, projectiles will be cancelled without teleportation")
                .define("teleport_enabled", false);
        this.projectileDodgeDurationCost = builder
                .comment("Duration cost per dodge (in ticks)", "每次闪避消耗的持续时间（tick）",
                        "Default: 200 ticks (10 seconds)")
                .defineInRange("duration_cost", 200, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("vitality").comment("生机");
        this.vitalityBlacklist = builder
                .comment("List of entity IDs that cannot be affected by Vitality effect", "生机效果黑名单实体ID",
                        "Format: modid:entity_id (e.g., minecraft:zombie, minecraft:villager)")
                .defineList("blacklist", Collections.emptyList(), MainConfig::validateEntityName);
        builder.pop();

        builder.pop();
        builder.pop();


        builder.push("farmersdelight").comment("农夫乐事");
        this.cookingPotRecipesDisabled = builder
                .comment("Whether all FarmersDelight cooking pot recipes are disabled", "是否禁用农夫乐事厨锅配方",
                        "When enabled, all cooking pot recipes will not work")
                .define("cooking_pot_recipes_disabled", false);
        this.cookingPotGuiDisabled = builder
                .comment("Whether the FarmersDelight cooking pot GUI is disabled", "是否禁用农夫乐事厨锅",
                        "When enabled, the cooking pot GUI will not open")
                .define("cooking_pot_gui_disabled", false);
        this.cuttingBoardRecipesDisabled = builder
                .comment("Whether all FarmersDelight cutting board recipes are disabled", "是否禁用农夫乐事砧板配方",
                        "When enabled, all cutting board recipes will not work")
                .define("cutting_board_recipes_disabled", false);
        this.richSoilHoeEnabled = builder
                .comment("Whether rich soil hoe tilling is enabled", "是否启用水中锄耕农夫乐事沃土功能",
                        "When enabled, right-clicking rich soil with water above will turn it into rich soil farmland")
                .define("hoe_enabled", true);
        builder.pop();


        builder.push("youkaisfeasts").comment("幻想乡乐事");
        this.steamingRecipesDisabled = builder
                .comment("Whether all Youkai's Homecoming steaming recipes are disabled", "是否禁用妖怪归家蒸笼配方",
                        "When enabled, all steaming recipes will not work")
                .define("steaming_recipes_disabled", false);
        builder.pop();


        builder.push("farm_and_charm").comment("沉浸农艺");
        this.farmAndCharmCookingPotRecipesDisabled = builder
                .comment("Whether all Farm and Charm cooking pot recipes are disabled", "是否禁用沉浸农艺厨锅配方",
                        "When enabled, all Farm and Charm cooking pot recipes will not work")
                .define("cooking_pot_recipes_disabled", false);
        this.farmAndCharmCompatEnabled = builder
                .comment("Whether Farm and Charm mod compatibility is enabled", "是否启用沉浸农艺锅配方兼容")
                .define("farm_and_charm_compat_enabled", true);
        builder.pop();


        builder.push("vinery").comment("葡园酒香");
        this.vineryBarrelRecipesDisabled = builder
                .comment("Whether all Vinery fermentation barrel recipes are disabled", "是否禁用葡园酒香酿造桶配方",
                        "When enabled, all Vinery fermentation barrel recipes will not work")
                .define("barrel_recipes_disabled", false);
        builder.pop();


        builder.push("little_maid").comment("车万女仆");
        this.littleMaidCompatEnabled = builder
                .comment("Whether Little Maid mod compatibility is enabled", "是否启用女仆任务拓展模块",
                        "When disabled, all Little Maid compatibility features will not be loaded")
                .define("little_maid_compat_enabled", true);
        this.littleMaidChoppingBoardEnabled = builder
                .comment("Whether Little Maid chopping board task is enabled", "是否启用女仆菜板任务",
                        "When enabled, maids can cut ingredients on the chopping board")
                .define("chopping_board_enabled", true);
        this.littleMaidMillstoneEnabled = builder
                .comment("Whether Little Maid millstone task is enabled", "是否启用女仆石磨任务",
                        "When enabled, maids can grind ingredients on the millstone")
                .define("millstone_enabled", true);
        this.littleMaidPressingTubEnabled = builder
                .comment("Whether Little Maid pressing tub task is enabled", "是否启用女仆果盆任务",
                        "When enabled, maids can press ingredients in the pressing tub")
                .define("pressing_tub_enabled", true);
        builder.pop();


        builder.push("spectrum").comment("光谱世界");
        this.spectrumCompatEnabled = builder
                .comment("Whether Spectrum mod compatibility is enabled", "是否启用光谱世界兼容模块",
                        "When disabled, all Spectrum compatibility features will not be loaded")
                .define("spectrum_compat_enabled", true);
        this.spectrumPotItemHandlerEnabled = builder
                .comment("Whether Spectrum pot item handler is enabled", "是否启用炒锅节点传输",
                        "When enabled, Spectrum mod can interact with the pot via item handler")
                .define("pot_item_handler_enabled", true);
        this.spectrumChoppingBoardItemHandlerEnabled = builder
                .comment("Whether Spectrum chopping board item handler is enabled", "是否启用切菜板节点传输",
                        "When enabled, Spectrum mod can interact with the chopping board via item handler")
                .define("chopping_board_item_handler_enabled", true);
        this.spectrumMillstoneItemHandlerEnabled = builder
                .comment("Whether Spectrum millstone item handler is enabled", "是否启用石磨节点传输",
                        "When enabled, Spectrum mod can interact with the millstone via item handler")
                .define("millstone_item_handler_enabled", true);
        this.spectrumShawarmaSpitItemHandlerEnabled = builder
                .comment("Whether Spectrum shawarma spit item handler is enabled", "是否启用旋风烤肉塔节点传输",
                        "When enabled, Spectrum mod can interact with the shawarma spit via item handler")
                .define("shawarma_spit_item_handler_enabled", true);
        this.spectrumSteamerItemHandlerEnabled = builder
                .comment("Whether Spectrum steamer item handler is enabled", "是否启用蒸笼节点传输",
                        "When enabled, Spectrum mod can interact with the steamer via item handler")
                .define("steamer_item_handler_enabled", true);
        this.spectrumTeapotItemHandlerEnabled = builder
                .comment("Whether Spectrum teapot item handler is enabled", "是否启用茶壶节点传输",
                        "When enabled, Spectrum mod can interact with the teapot via item handler")
                .define("teapot_item_handler_enabled", true);
        this.spectrumTrashCanItemHandlerEnabled = builder
                .comment("Whether Spectrum trash can item handler is enabled", "是否启用垃圾桶节点传输",
                        "When enabled, Spectrum mod can interact with the trash can via item handler")
                .define("trash_can_item_handler_enabled", true);
        this.spectrumPastelNodeCompatEnabled = builder
                .comment("Whether Spectrum pastel node pot oil compatibility is enabled", "是否启用节点炒锅加油",
                        "When enabled, pastel nodes can add oil to pots")
                .define("pastel_node_compat_enabled", true);
        builder.pop();


        builder.push("create").comment("机械动力");
        this.createCompatEnabled = builder
                .comment("Whether Create mod compatibility is enabled", "是否启用机械动力兼容模块",
                        "When disabled, all Create compatibility features will not be loaded")
                .define("create_compat_enabled", true);
        this.createArmPotEnabled = builder
                .comment("Whether Create mechanical arm pot compatibility is enabled", "是否启用机械动力动力臂炒锅兼容")
                .define("arm_pot_enabled", true);
        this.createArmStockpotEnabled = builder
                .comment("Whether Create mechanical arm stockpot compatibility is enabled", "是否启用机械动力动力臂汤锅兼容")
                .define("arm_stockpot_enabled", true);
        this.createArmSteamerEnabled = builder
                .comment("Whether Create mechanical arm steamer compatibility is enabled", "是否启用机械动力动力臂蒸笼兼容")
                .define("arm_steamer_enabled", true);
        this.createArmMillstoneEnabled = builder
                .comment("Whether Create mechanical arm millstone compatibility is enabled", "是否启用机械动力动力臂磨盘兼容")
                .define("arm_millstone_enabled", true);
        this.createArmShawarmaSpitEnabled = builder
                .comment("Whether Create mechanical arm shawarma spit compatibility is enabled", "是否启用机械动力动力臂烤肉塔兼容")
                .define("arm_shawarma_spit_enabled", true);
        this.createArmTeapotEnabled = builder
                .comment("Whether Create mechanical arm teapot compatibility is enabled", "是否启用机械动力动力臂茶壶兼容")
                .define("arm_teapot_enabled", true);
        builder.pop();


        builder.push("compat").comment("功能设置");
        this.jeiCompatEnabled = builder
                .comment("Whether JEI compatibility is enabled", "是否启用旋风烤肉塔JEI")
                .define("jei_compat_enabled", true);
        this.thirstCompatEnabled = builder
                .comment("Whether Thirst mod compatibility is enabled", "是否启用口渴兼容")
                .define("thirst_compat_enabled", true);
        this.quarkSickleHarvestFixEnabled = builder
                .comment("Whether Quark sickle harvest fix is enabled", "是否启用夸克兼容",
                        "When enabled, sickle items will not trigger Quark's automatic harvest")
                .define("quark_sickle_harvest_fix_enabled", true);
        builder.pop();
    }

    public static DatapackMode datapackModeValue = DatapackMode.COMPAT;
    public static boolean soupDatapackEnabledValue = true;
    public static boolean millstoneStackingEnabledValue = true;
    public static boolean lunchBagBlacklistEnabledValue = true;
    public static Set<ResourceLocation> lunchBagBlacklistValue = new HashSet<>();
    public static boolean transmutationLunchBagBackEnabledValue = true;
    public static boolean appleskinCompatEnabledValue = true;
    public static boolean scarecrowRepelPhantomsValue = true;
    public static boolean nourishmentEffectBlockEnabledValue = true;
    public static boolean projectileDodgeTeleportEnabledValue = false;
    public static int projectileDodgeDurationCostValue = 200;
    public static Set<ResourceLocation> vitalityBlacklistValue = new HashSet<>();
    public static boolean cookingPotRecipesDisabledValue = false;
    public static boolean cuttingBoardRecipesDisabledValue = false;
    public static boolean richSoilHoeEnabledValue = true;
    public static boolean farmAndCharmCookingPotRecipesDisabledValue = false;
    public static boolean farmAndCharmCompatEnabledValue = true;
    public static boolean vineryBarrelRecipesDisabledValue = false;
    public static boolean littleMaidCompatEnabledValue = true;
    public static boolean littleMaidChoppingBoardEnabledValue = true;
    public static boolean littleMaidMillstoneEnabledValue = true;
    public static boolean littleMaidPressingTubEnabledValue = true;
    public static boolean spectrumCompatEnabledValue = true;
    public static boolean spectrumMillstoneItemHandlerEnabledValue = true;
    public static boolean spectrumPastelNodeCompatEnabledValue = true;
    public static boolean spectrumSteamerItemHandlerEnabledValue = true;
    public static boolean spectrumTrashCanItemHandlerEnabledValue = true;
    public static boolean spectrumChoppingBoardItemHandlerEnabledValue = true;
    public static boolean spectrumTeapotItemHandlerEnabledValue = true;
    public static boolean spectrumShawarmaSpitItemHandlerEnabledValue = true;
    public static boolean spectrumPotItemHandlerEnabledValue = true;
    public static boolean createCompatEnabledValue = true;
    public static boolean createArmPotEnabledValue = true;
    public static boolean createArmStockpotEnabledValue = true;
    public static boolean createArmSteamerEnabledValue = true;
    public static boolean createArmMillstoneEnabledValue = true;
    public static boolean createArmShawarmaSpitEnabledValue = true;
    public static boolean createArmTeapotEnabledValue = true;
    public static boolean jeiCompatEnabledValue = true;
    public static boolean thirstCompatEnabledValue = true;
    public static boolean quarkSickleHarvestFixEnabledValue = true;
    public static boolean steamingRecipesDisabledValue = false;
    public static boolean cookingPotGuiDisabledValue = false;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        datapackModeValue = CONFIG.datapackMode.get();
        soupDatapackEnabledValue = CONFIG.soupDatapackEnabled.get();
        millstoneStackingEnabledValue = CONFIG.millstoneStackingEnabled.get();
        lunchBagBlacklistEnabledValue = CONFIG.lunchBagBlacklistEnabled.get();
        transmutationLunchBagBackEnabledValue = CONFIG.transmutationLunchBagBackEnabled.get();
        appleskinCompatEnabledValue = CONFIG.appleskinCompatEnabled.get();
        lunchBagBlacklistValue.clear();
        for (String itemStr : CONFIG.lunchBagBlacklist.get()) {
            ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
            if (itemId != null) {
                lunchBagBlacklistValue.add(itemId);
            }
        }
        scarecrowRepelPhantomsValue = CONFIG.scarecrowRepelPhantoms.get();
        nourishmentEffectBlockEnabledValue = CONFIG.nourishmentEffectBlockEnabled.get();
        projectileDodgeTeleportEnabledValue = CONFIG.projectileDodgeTeleportEnabled.get();
        projectileDodgeDurationCostValue = CONFIG.projectileDodgeDurationCost.get();
        vitalityBlacklistValue.clear();
        for (String entityStr : CONFIG.vitalityBlacklist.get()) {
            ResourceLocation entityId = ResourceLocation.tryParse(entityStr);
            if (entityId != null) {
                vitalityBlacklistValue.add(entityId);
            }
        }
        cookingPotRecipesDisabledValue = CONFIG.cookingPotRecipesDisabled.get();
        cuttingBoardRecipesDisabledValue = CONFIG.cuttingBoardRecipesDisabled.get();
        richSoilHoeEnabledValue = CONFIG.richSoilHoeEnabled.get();
        farmAndCharmCookingPotRecipesDisabledValue = CONFIG.farmAndCharmCookingPotRecipesDisabled.get();
        farmAndCharmCompatEnabledValue = CONFIG.farmAndCharmCompatEnabled.get();
        vineryBarrelRecipesDisabledValue = CONFIG.vineryBarrelRecipesDisabled.get();
        littleMaidCompatEnabledValue = CONFIG.littleMaidCompatEnabled.get();
        littleMaidChoppingBoardEnabledValue = CONFIG.littleMaidChoppingBoardEnabled.get();
        littleMaidMillstoneEnabledValue = CONFIG.littleMaidMillstoneEnabled.get();
        littleMaidPressingTubEnabledValue = CONFIG.littleMaidPressingTubEnabled.get();
        spectrumCompatEnabledValue = CONFIG.spectrumCompatEnabled.get();
        spectrumMillstoneItemHandlerEnabledValue = CONFIG.spectrumMillstoneItemHandlerEnabled.get();
        spectrumPastelNodeCompatEnabledValue = CONFIG.spectrumPastelNodeCompatEnabled.get();
        spectrumSteamerItemHandlerEnabledValue = CONFIG.spectrumSteamerItemHandlerEnabled.get();
        spectrumTrashCanItemHandlerEnabledValue = CONFIG.spectrumTrashCanItemHandlerEnabled.get();
        spectrumChoppingBoardItemHandlerEnabledValue = CONFIG.spectrumChoppingBoardItemHandlerEnabled.get();
        spectrumTeapotItemHandlerEnabledValue = CONFIG.spectrumTeapotItemHandlerEnabled.get();
        spectrumShawarmaSpitItemHandlerEnabledValue = CONFIG.spectrumShawarmaSpitItemHandlerEnabled.get();
        spectrumPotItemHandlerEnabledValue = CONFIG.spectrumPotItemHandlerEnabled.get();
        createCompatEnabledValue = CONFIG.createCompatEnabled.get();
        createArmPotEnabledValue = CONFIG.createArmPotEnabled.get();
        createArmStockpotEnabledValue = CONFIG.createArmStockpotEnabled.get();
        createArmSteamerEnabledValue = CONFIG.createArmSteamerEnabled.get();
        createArmMillstoneEnabledValue = CONFIG.createArmMillstoneEnabled.get();
        createArmShawarmaSpitEnabledValue = CONFIG.createArmShawarmaSpitEnabled.get();
        createArmTeapotEnabledValue = CONFIG.createArmTeapotEnabled.get();
        jeiCompatEnabledValue = CONFIG.jeiCompatEnabled.get();
        thirstCompatEnabledValue = CONFIG.thirstCompatEnabled.get();
        quarkSickleHarvestFixEnabledValue = CONFIG.quarkSickleHarvestFixEnabled.get();
        steamingRecipesDisabledValue = CONFIG.steamingRecipesDisabled.get();
        cookingPotGuiDisabledValue = CONFIG.cookingPotGuiDisabled.get();
    }

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemStr)) {
            return false;
        }
        return ResourceLocation.tryParse(itemStr) != null;
    }

    public static boolean isItemBlacklisted(Item item) {
        if (!lunchBagBlacklistEnabledValue) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return lunchBagBlacklistValue.contains(itemId);
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

    static {
        Pair<MainConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(MainConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}