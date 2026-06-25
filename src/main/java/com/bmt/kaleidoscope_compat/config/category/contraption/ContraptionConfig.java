package com.bmt.kaleidoscope_compat.config.category.contraption;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

/**
 * 动态结构兼容配置
 * 控制各个方块在 Create 动态结构上的交互行为
 */
@ConfigObject
@SuppressWarnings("all")
public final class ContraptionConfig {

    @ConfigEntry(id = "pot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.pot_enabled")
    @Comment(
            value = "Whether to enable pot contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.pot_enabled.comment"
    )
    public static boolean potEnabled = true;

    @ConfigEntry(id = "stove_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.stove_enabled")
    @Comment(
            value = "Whether to enable stove contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.stove_enabled.comment"
    )
    public static boolean stoveEnabled = true;

    @ConfigEntry(id = "stockpot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.stockpot_enabled")
    @Comment(
            value = "Whether to enable stockpot contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.stockpot_enabled.comment"
    )
    public static boolean stockpotEnabled = true;

    @ConfigEntry(id = "steamer_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.steamer_enabled")
    @Comment(
            value = "Whether to enable steamer contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.steamer_enabled.comment"
    )
    public static boolean steamerEnabled = true;

    @ConfigEntry(id = "teapot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.teapot_enabled")
    @Comment(
            value = "Whether to enable teapot contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.teapot_enabled.comment"
    )
    public static boolean teapotEnabled = true;

    @ConfigEntry(id = "fruit_basket_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.fruit_basket_enabled")
    @Comment(
            value = "Whether to enable fruit basket contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.fruit_basket_enabled.comment"
    )
    public static boolean fruitBasketEnabled = true;

    @ConfigEntry(id = "chopping_board_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.chopping_board_enabled")
    @Comment(
            value = "Whether to enable chopping board contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.chopping_board_enabled.comment"
    )
    public static boolean choppingBoardEnabled = true;

    @ConfigEntry(id = "enamel_basin_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.enamel_basin_enabled")
    @Comment(
            value = "Whether to enable enamel basin contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.enamel_basin_enabled.comment"
    )
    public static boolean enamelBasinEnabled = true;

    @ConfigEntry(id = "kitchenware_racks_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.kitchenware_racks_enabled")
    @Comment(
            value = "Whether to enable kitchenware racks contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.kitchenware_racks_enabled.comment"
    )
    public static boolean kitchenwareRacksEnabled = true;

    @ConfigEntry(id = "millstone_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.millstone_enabled")
    @Comment(
            value = "Whether to enable millstone contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.millstone_enabled.comment"
    )
    public static boolean millstoneEnabled = true;

    @ConfigEntry(id = "oil_pot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.oil_pot_enabled")
    @Comment(
            value = "Whether to enable oil pot contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.oil_pot_enabled.comment"
    )
    public static boolean oilPotEnabled = true;

    @ConfigEntry(id = "shawarma_spit_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.shawarma_spit_enabled")
    @Comment(
            value = "Whether to enable shawarma spit contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.shawarma_spit_enabled.comment"
    )
    public static boolean shawarmaSpitEnabled = true;

    @ConfigEntry(id = "trash_can_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.trash_can_enabled")
    @Comment(
            value = "Whether to enable trash can contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.trash_can_enabled.comment"
    )
    public static boolean trashCanEnabled = true;

    @ConfigEntry(id = "table_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.table_enabled")
    @Comment(
            value = "Whether to enable table contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.table_enabled.comment"
    )
    public static boolean tableEnabled = true;

    @ConfigEntry(id = "chair_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.chair_enabled")
    @Comment(
            value = "Whether to enable chair contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.chair_enabled.comment"
    )
    public static boolean chairEnabled = true;

    @ConfigEntry(id = "cook_stool_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.contraption.cook_stool_enabled")
    @Comment(
            value = "Whether to enable cook stool contraption interaction",
            translation = "config.kaleidoscope_compat.create.contraption.cook_stool_enabled.comment"
    )
    public static boolean cookStoolEnabled = true;
}