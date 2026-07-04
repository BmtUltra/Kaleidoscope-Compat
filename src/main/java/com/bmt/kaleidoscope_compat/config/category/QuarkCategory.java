package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("quark")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.quark",
        descriptionTranslation = "config.kaleidoscope_compat.category.quark.description",
        icon = "feather",
        links = {
                @ConfigInfo.Link(
                        value = "https://www.curseforge.com/minecraft/mc-mods/quark",
                        icon = "curseforge",
                        text = "CurseForge",
                        textTranslation = "config.kaleidoscope_compat.links.curseforge"
                ),
                @ConfigInfo.Link(
                        value = "https://modrinth.com/mod/quark",
                        icon = "modrinth",
                        text = "Modrinth",
                        textTranslation = "config.kaleidoscope_compat.links.modrinth"
                ),
                @ConfigInfo.Link(
                        value = "https://github.com/VazkiiMods/Quark",
                        icon = "clipboard_list",
                        text = "GitHub",
                        textTranslation = "config.kaleidoscope_compat.links.github"
                )
        }
)
@SuppressWarnings("all")
public final class QuarkCategory {

    @ConfigEntry(id = "sickle_harvest_fix_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.quark.sickle_harvest_fix_enabled")
    @Comment(
            value = "Whether Quark sickle harvest fix is enabled",
            translation = "config.kaleidoscope_compat.quark.sickle_harvest_fix_enabled.comment"
    )
    public static boolean quarkSickleHarvestFixEnabled = true;
}