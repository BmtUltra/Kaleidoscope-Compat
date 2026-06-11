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
        icon = "sparkles",
        links = {
                @ConfigInfo.Link(
                        value = "https://www.curseforge.com/minecraft/mc-mods/spectrum",
                        icon = "curseforge",
                        text = "CurseForge",
                        textTranslation = "config.kaleidoscope_compat.links.curseforge"
                ),
                @ConfigInfo.Link(
                        value = "https://modrinth.com/mod/spectrum",
                        icon = "modrinth",
                        text = "Modrinth",
                        textTranslation = "config.kaleidoscope_compat.links.modrinth"
                ),
                @ConfigInfo.Link(
                        value = "https://github.com/DaFuqs/Spectrum",
                        icon = "clipboard_list",
                        text = "GitHub",
                        textTranslation = "config.kaleidoscope_compat.links.github"
                )
        }
)
@SuppressWarnings("all")
public final class SpectrumCategory {

    @ConfigEntry(id = "compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.compat_enabled")
    @Comment(
            value = "Whether Spectrum mod compatibility is enabled",
            translation = "config.kaleidoscope_compat.spectrum.compat_enabled.comment"
    )
    public static boolean spectrumCompatEnabled = true;

    @ConfigEntry(id = "pot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.pot_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum pot item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.pot_item_handler_enabled.comment"
    )
    public static boolean spectrumPotItemHandlerEnabled = true;

    @ConfigEntry(id = "chopping_board_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.chopping_board_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum chopping board item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.chopping_board_item_handler_enabled.comment"
    )
    public static boolean spectrumChoppingBoardItemHandlerEnabled = true;

    @ConfigEntry(id = "millstone_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.millstone_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum millstone item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.millstone_item_handler_enabled.comment"
    )
    public static boolean spectrumMillstoneItemHandlerEnabled = true;

    @ConfigEntry(id = "shawarma_spit_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.shawarma_spit_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum shawarma spit item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.shawarma_spit_item_handler_enabled.comment"
    )
    public static boolean spectrumShawarmaSpitItemHandlerEnabled = true;

    @ConfigEntry(id = "steamer_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.steamer_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum steamer item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.steamer_item_handler_enabled.comment"
    )
    public static boolean spectrumSteamerItemHandlerEnabled = true;

    @ConfigEntry(id = "teapot_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.teapot_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum teapot item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.teapot_item_handler_enabled.comment"
    )
    public static boolean spectrumTeapotItemHandlerEnabled = true;

    @ConfigEntry(id = "trash_can_item_handler_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.trash_can_item_handler_enabled")
    @Comment(
            value = "Whether Spectrum trash can item handler is enabled",
            translation = "config.kaleidoscope_compat.spectrum.trash_can_item_handler_enabled.comment"
    )
    public static boolean spectrumTrashCanItemHandlerEnabled = true;

    @ConfigEntry(id = "pastel_node_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.pastel_node_compat_enabled")
    @Comment(
            value = "Whether Spectrum pastel node pot oil compatibility is enabled",
            translation = "config.kaleidoscope_compat.spectrum.pastel_node_compat_enabled.comment"
    )
    public static boolean spectrumPastelNodeCompatEnabled = true;
}