package com.bmt.kaleidoscope_compat.config;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ClothConfig {
    public static void build(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> {
            ConfigBuilder configBuilder = ConfigBuilder.create();
            ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
            configBuilder.setTitle(Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".title"));
            configBuilder.setParentScreen(parent);

            ConfigCategory datapackCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.datapack"));

            datapackCategory.addEntry(entryBuilder
                    .startEnumSelector(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".datapack.mode"),
                            DatapackMode.class,
                            MainConfig.datapackMode)
                    .setDefaultValue(DatapackMode.COMPAT)
                    .setSaveConsumer(newValue -> MainConfig.datapackMode = newValue)
                    .setEnumNameProvider(enumValue -> {
                        if (enumValue instanceof DatapackMode mode) {
                            return mode.getDisplayName();
                        }
                        return Component.literal(enumValue.toString());
                    })
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".datapack.mode.tooltip.1"),
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".datapack.mode.tooltip.2")
                    )
                    .build());

            datapackCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".datapack.soup_enabled"),
                            MainConfig.soupDatapackEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.soupDatapackEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".datapack.soup_enabled.tooltip.0")
                    )
                    .build());

            ConfigCategory kitchenCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.kitchen"));

            // 嬗变饭袋子分类
            var lunchBagSubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".kitchen.lunch_bag_subcategory"));

            lunchBagSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".lunchBlacklist.enabled"),
                            MainConfig.lunchBagBlacklistEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.lunchBagBlacklistEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".lunchBlacklist.enabled.tooltip.0")
                    )
                    .build());

            lunchBagSubCategory.add(entryBuilder
                    .startStrList(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".lunchBlacklist.list"),
                            MainConfig.lunchBagBlacklist.stream()
                                    .map(ResourceLocation::toString)
                                    .collect(Collectors.toList()))
                    .setDefaultValue(List.of("artifacts:eternal_steak", "kaleidoscope_nether:everlasting_flame_steak"))
                    .setSaveConsumer(newValue -> {
                        MainConfig.lunchBagBlacklist.clear();
                        for (String s : newValue) {
                            ResourceLocation id = ResourceLocation.tryParse(s);
                            if (id != null) {
                                MainConfig.lunchBagBlacklist.add(id);
                            }
                        }
                    })
                    .build());

            lunchBagSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".transmutationLunchBag.back_behavior"),
                            MainConfig.transmutationLunchBagBackEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.transmutationLunchBagBackEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".transmutationLunchBag.back_behavior.tooltip.0")
                    )
                    .build());

            lunchBagSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".appleskin.compat_enabled"),
                            MainConfig.appleskinCompatEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.appleskinCompatEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".appleskin.compat_enabled.tooltip.0")
                    )
                    .build());

            kitchenCategory.addEntry(lunchBagSubCategory.build());

            // 稻草人子分类
            var scarecrowSubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".kitchen.scarecrow_subcategory"));

            scarecrowSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".scarecrow.repel_phantoms"),
                            MainConfig.scarecrowRepelPhantoms)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.scarecrowRepelPhantoms = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".scarecrow.repel_phantoms.tooltip.0")
                    )
                    .build());

            kitchenCategory.addEntry(scarecrowSubCategory.build());

            // 效果设置子分类
            var effectSubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".kitchen.effect_subcategory"));

            effectSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".nourishmentEffect.block_enabled"),
                            MainConfig.nourishmentEffectBlockEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.nourishmentEffectBlockEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".nourishmentEffect.block_enabled.tooltip.0")
                    )
                    .build());

            var projectileDodgeSubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".effect.projectile_dodge_subcategory"));

            projectileDodgeSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".projectileDodge.teleport_enabled"),
                            MainConfig.projectileDodgeTeleportEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.projectileDodgeTeleportEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".projectileDodge.teleport_enabled.tooltip.0")
                    )
                    .build());

            projectileDodgeSubCategory.add(entryBuilder
                    .startIntField(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".projectileDodge.duration_cost"),
                            MainConfig.projectileDodgeDurationCost)
                    .setDefaultValue(200)
                    .setMin(1)
                    .setMax(Integer.MAX_VALUE)
                    .setSaveConsumer(newValue -> MainConfig.projectileDodgeDurationCost = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".projectileDodge.duration_cost.tooltip.0"),
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".projectileDodge.duration_cost.tooltip.1")
                    )
                    .build());

            effectSubCategory.add(projectileDodgeSubCategory.build());

            var vitalitySubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".effect.vitality_subcategory"));

            vitalitySubCategory.add(entryBuilder
                    .startStrList(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".vitality.blacklist"),
                            MainConfig.vitalityBlacklist.stream()
                                    .map(ResourceLocation::toString)
                                    .collect(Collectors.toList()))
                    .setDefaultValue(List.of())
                    .setSaveConsumer(newValue -> {
                        MainConfig.vitalityBlacklist.clear();
                        for (String s : newValue) {
                            ResourceLocation id = ResourceLocation.tryParse(s);
                            if (id != null) {
                                MainConfig.vitalityBlacklist.add(id);
                            }
                        }
                    })
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".vitality.blacklist.tooltip.0")
                    )
                    .build());

            effectSubCategory.add(vitalitySubCategory.build());

            kitchenCategory.addEntry(effectSubCategory.build());

            ConfigCategory farmersDelightCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.farmersdelight"));

            farmersDelightCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farmersdelight.cooking_pot_disabled"),
                            MainConfig.cookingPotRecipesDisabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.cookingPotRecipesDisabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farmersdelight.cooking_pot_disabled.tooltip.0")
                    )
                    .build());

            farmersDelightCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farmersdelight.cutting_board_disabled"),
                            MainConfig.cuttingBoardRecipesDisabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.cuttingBoardRecipesDisabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farmersdelight.cutting_board_disabled.tooltip.0")
                    )
                    .build());

            farmersDelightCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".rich_soil.hoe_enabled"),
                            MainConfig.richSoilHoeEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.richSoilHoeEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".rich_soil.hoe_enabled.tooltip.0")
                    )
                    .build());

            ConfigCategory youkaisfeastsCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.youkaisfeasts"));

            youkaisfeastsCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".youkaisfeasts.steaming_recipes_disabled"),
                            MainConfig.steamingRecipesDisabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.steamingRecipesDisabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".youkaisfeasts.steaming_recipes_disabled.tooltip.0")
                    )
                    .build());

            ConfigCategory farmAndCharmCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.farm_and_charm"));

            farmAndCharmCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farm_and_charm.cooking_pot_disabled"),
                            MainConfig.farmAndCharmCookingPotRecipesDisabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.farmAndCharmCookingPotRecipesDisabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farm_and_charm.cooking_pot_disabled.tooltip.0")
                    )
                    .build());

            farmAndCharmCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farm_and_charm.compat_enabled"),
                            MainConfig.farmAndCharmCompatEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.farmAndCharmCompatEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".farm_and_charm.compat_enabled.tooltip.0")
                    )
                    .build());

            ConfigCategory vineryCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.vinery"));

            vineryCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".vinery.barrel_disabled"),
                            MainConfig.vineryBarrelRecipesDisabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> MainConfig.vineryBarrelRecipesDisabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".vinery.barrel_disabled.tooltip.0")
                    )
                    .build());

            ConfigCategory createCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.create"));

            createCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.compat_enabled"),
                            MainConfig.createCompatEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createCompatEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.compat_enabled.tooltip.0")
                    )
                    .build());

            var armSubCategory = entryBuilder.startSubCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_subcategory"));

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_pot_enabled"),
                            MainConfig.createArmPotEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmPotEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_pot_enabled.tooltip.0")
                    )
                    .build());

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_stockpot_enabled"),
                            MainConfig.createArmStockpotEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmStockpotEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_stockpot_enabled.tooltip.0")
                    )
                    .build());

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_steamer_enabled"),
                            MainConfig.createArmSteamerEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmSteamerEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_steamer_enabled.tooltip.0")
                    )
                    .build());

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_millstone_enabled"),
                            MainConfig.createArmMillstoneEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmMillstoneEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_millstone_enabled.tooltip.0")
                    )
                    .build());

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_shawarma_spit_enabled"),
                            MainConfig.createArmShawarmaSpitEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmShawarmaSpitEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_shawarma_spit_enabled.tooltip.0")
                    )
                    .build());

            armSubCategory.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_teapot_enabled"),
                            MainConfig.createArmTeapotEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.createArmTeapotEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".create.arm_teapot_enabled.tooltip.0")
                    )
                    .build());

            createCategory.addEntry(armSubCategory.build());

            ConfigCategory compatCategory = configBuilder.getOrCreateCategory(
                    Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".category.compat"));

            compatCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".jei.compat_enabled"),
                            MainConfig.jeiCompatEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.jeiCompatEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".jei.compat_enabled.tooltip.0")
                    )
                    .build());

            compatCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".thirst.compat_enabled"),
                            MainConfig.thirstCompatEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.thirstCompatEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".thirst.compat_enabled.tooltip.0")
                    )
                    .build());

            compatCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".quark.sickle_harvest_fix_enabled"),
                            MainConfig.quarkSickleHarvestFixEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> MainConfig.quarkSickleHarvestFixEnabled = newValue)
                    .setTooltip(
                            Component.translatable("config." + KaleidoscopeCompat.MOD_ID + ".quark.sickle_harvest_fix_enabled.tooltip.0")
                    )
                    .build());

            return configBuilder.build();
        });
    }
}