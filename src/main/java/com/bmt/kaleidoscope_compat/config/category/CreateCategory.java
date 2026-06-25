package com.bmt.kaleidoscope_compat.config.category;

import com.bmt.kaleidoscope_compat.config.category.arm.ArmConfig;
import com.bmt.kaleidoscope_compat.config.category.contraption.ContraptionConfig;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("create")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.create",
        descriptionTranslation = "config.kaleidoscope_compat.category.create.description",
        icon = "cog",
links = {
@ConfigInfo.Link(
        value = "https://www.curseforge.com/minecraft/mc-mods/create",
        icon = "curseforge",
        text = "CurseForge",
        textTranslation = "config.kaleidoscope_compat.links.curseforge"
),
@ConfigInfo.Link(
        value = "https://modrinth.com/mod/create",
        icon = "modrinth",
        text = "Modrinth",
        textTranslation = "config.kaleidoscope_compat.links.modrinth"
),
@ConfigInfo.Link(
        value = "https://github.com/Creators-of-Create/Create",
        icon = "clipboard_list",
        text = "GitHub",
        textTranslation = "config.kaleidoscope_compat.links.github"
)
        }
)
@SuppressWarnings("all")
public final class CreateCategory {

    @ConfigEntry(id = "compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.compat_enabled")
    @Comment(
            value = "Whether Create mod compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.compat_enabled.comment"
    )
    public static boolean createCompatEnabled = true;

    @ConfigEntry(id = "arm", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.create.arm")
    @Comment(
            value = "Mechanical arm related settings",
            translation = "config.kaleidoscope_compat.create.arm.comment"
    )
    public static final ArmConfig arm = new ArmConfig();

    @ConfigEntry(id = "contraption", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.create.contraption")
    @Comment(
            value = "Contraption interaction related settings",
            translation = "config.kaleidoscope_compat.create.contraption.comment"
    )
    public static final ContraptionConfig contraption = new ContraptionConfig();
}
