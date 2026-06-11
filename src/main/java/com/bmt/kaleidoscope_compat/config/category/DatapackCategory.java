package com.bmt.kaleidoscope_compat.config.category;

import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("datapack")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.datapack",
        descriptionTranslation = "config.kaleidoscope_compat.category.datapack.description",
        icon = "package",
        links = {
                @ConfigInfo.Link(
                        value = "https://example.com/datapack-wiki",
                        icon = "book-open",
                        text = "Wiki",
                        textTranslation = "config.kaleidoscope_compat.links.datapack_docs"
                )
        }
)
@SuppressWarnings("all")
public final class DatapackCategory {
    @ConfigEntry(
            id = "datapack_mode",
            type = EntryType.ENUM,
            translation = "config.kaleidoscope_compat.datapack.mode"
    )
    @Comment(
            value = "NONE: Disable all datapacks except soup\nCOMPAT: Extensive compatibility with other mod items\nUNITE: Duplicate items of the unified module",
            translation = "config.kaleidoscope_compat.datapack.mode.comment"
    )
    public static DatapackMode datapackMode = DatapackMode.COMPAT;

    @ConfigEntry(id = "soup_datapack_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.datapack.soup_enabled")
    @Comment(
            value = "Enable soup base material",
            translation = "config.kaleidoscope_compat.datapack.soup_enabled.comment"
    )
    public static boolean soupDatapackEnabled = true;
}