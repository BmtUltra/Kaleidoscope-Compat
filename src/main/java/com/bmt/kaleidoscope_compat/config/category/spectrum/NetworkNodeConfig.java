package com.bmt.kaleidoscope_compat.config.category.spectrum;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class NetworkNodeConfig {

    @ConfigEntry(id = "pot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.pot_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum pot item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.pot_item_handler_enabled.comment"
    )
    public static boolean potItemHandlerEnabled = true;

    @ConfigEntry(id = "chopping_board_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.chopping_board_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum chopping board item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.chopping_board_item_handler_enabled.comment"
    )
    public static boolean choppingBoardItemHandlerEnabled = true;

    @ConfigEntry(id = "millstone_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.millstone_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum millstone item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.millstone_item_handler_enabled.comment"
    )
    public static boolean millstoneItemHandlerEnabled = true;

    @ConfigEntry(id = "shawarma_spit_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.shawarma_spit_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum shawarma spit item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.shawarma_spit_item_handler_enabled.comment"
    )
    public static boolean shawarmaSpitItemHandlerEnabled = true;

    @ConfigEntry(id = "steamer_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.steamer_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum steamer item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.steamer_item_handler_enabled.comment"
    )
    public static boolean steamerItemHandlerEnabled = true;

    @ConfigEntry(id = "teapot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.teapot_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum teapot item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.teapot_item_handler_enabled.comment"
    )
    public static boolean teapotItemHandlerEnabled = true;

    @ConfigEntry(id = "trash_can_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.network_node.trash_can_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum trash can item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.network_node.trash_can_item_handler_enabled.comment"
    )
    public static boolean trashCanItemHandlerEnabled = true;
}