package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("other")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.other",
        descriptionTranslation = "config.kaleidoscope_compat.category.other.description",
        icon = "settings",
links = {
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "curseforge",
        text = "CurseForge",
        textTranslation = "config.kaleidoscope_compat.links.curseforge"
),
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "modrinth",
        text = "Modrinth",
        textTranslation = "config.kaleidoscope_compat.links.modrinth"
),
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "clipboard_list",
        text = "GitHub",
        textTranslation = "config.kaleidoscope_compat.links.github"
)
        }
)
public final class OtherCategory {

    @ConfigEntry(id = "jei_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.compat.jei_compat_enabled")
    @Comment("Whether JEI compatibility is enabled")
    public static boolean jeiCompatEnabled = true;

    @ConfigEntry(id = "thirst_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.compat.thirst_compat_enabled")
    @Comment("Whether Thirst mod compatibility is enabled")
    public static boolean thirstCompatEnabled = true;

    @ConfigEntry(id = "quark_sickle_harvest_fix_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.compat.quark_sickle_harvest_fix_enabled")
    @Comment("Whether Quark sickle harvest fix is enabled")
    public static boolean quarkSickleHarvestFixEnabled = true;

    @ConfigEntry(id = "suppress_tag_load_errors", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.compat.suppress_tag_load_errors")
    @Comment("Whether to suppress tag file loading error logs")
    public static boolean suppressTagLoadErrors = false;
}