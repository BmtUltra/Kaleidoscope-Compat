package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("spectrum")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.spectrum",
        descriptionTranslation = "config.kaleidoscope_compat.category.spectrum.description",
        icon = "sparkles"
)
public final class SpectrumCategory {

    @ConfigEntry(id = "compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.compat_enabled")
    @Comment("Whether Spectrum mod compatibility is enabled")
    public static boolean spectrumCompatEnabled = true;

    @ConfigEntry(id = "pot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.pot_item_handler_enabled")
    @Comment("Whether Spectrum pot item handler is enabled")
    public static boolean spectrumPotItemHandlerEnabled = true;

    @ConfigEntry(id = "chopping_board_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.chopping_board_item_handler_enabled")
    @Comment("Whether Spectrum chopping board item handler is enabled")
    public static boolean spectrumChoppingBoardItemHandlerEnabled = true;

    @ConfigEntry(id = "millstone_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.millstone_item_handler_enabled")
    @Comment("Whether Spectrum millstone item handler is enabled")
    public static boolean spectrumMillstoneItemHandlerEnabled = true;

    @ConfigEntry(id = "shawarma_spit_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.shawarma_spit_item_handler_enabled")
    @Comment("Whether Spectrum shawarma spit item handler is enabled")
    public static boolean spectrumShawarmaSpitItemHandlerEnabled = true;

    @ConfigEntry(id = "steamer_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.steamer_item_handler_enabled")
    @Comment("Whether Spectrum steamer item handler is enabled")
    public static boolean spectrumSteamerItemHandlerEnabled = true;

    @ConfigEntry(id = "teapot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.teapot_item_handler_enabled")
    @Comment("Whether Spectrum teapot item handler is enabled")
    public static boolean spectrumTeapotItemHandlerEnabled = true;

    @ConfigEntry(id = "trash_can_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.trash_can_item_handler_enabled")
    @Comment("Whether Spectrum trash can item handler is enabled")
    public static boolean spectrumTrashCanItemHandlerEnabled = true;

    @ConfigEntry(id = "pastel_node_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.pastel_node_compat_enabled")
    @Comment("Whether Spectrum pastel node pot oil compatibility is enabled")
    public static boolean spectrumPastelNodeCompatEnabled = true;
}