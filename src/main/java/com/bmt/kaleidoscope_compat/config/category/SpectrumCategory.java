package com.bmt.kaleidoscope_compat.config.category;

import com.bmt.kaleidoscope_compat.config.category.spectrum.NetworkNodeConfig;
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

    @ConfigEntry(id = "millstone_anvil_crushing_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.millstone_anvil_crushing_enabled")
    @Comment(
            value = "Whether Spectrum anvil crushing recipes can be used in millstone",
            translation = "config.kaleidoscope_compat.spectrum.millstone_anvil_crushing_enabled.comment"
    )
    public static boolean spectrumMillstoneAnvilCrushingEnabled = true;

    @ConfigEntry(id = "network_node", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.spectrum.network_node")
    @Comment(
            value = "Network node related settings",
            translation = "config.kaleidoscope_compat.spectrum.network_node.comment"
    )
    public static final NetworkNodeConfig networkNode = new NetworkNodeConfig();

    @ConfigEntry(id = "pastel_node_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.spectrum.pastel_node_compat_enabled")
    @Comment(
            value = "Whether Spectrum pastel node pot oil compatibility is enabled",
            translation = "config.kaleidoscope_compat.spectrum.pastel_node_compat_enabled.comment"
    )
    public static boolean spectrumPastelNodeCompatEnabled = true;
}