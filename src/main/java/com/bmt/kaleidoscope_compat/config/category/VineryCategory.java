package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("vinery")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.vinery",
        descriptionTranslation = "config.kaleidoscope_compat.category.vinery.description",
        icon = "wine-bottle",
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
@SuppressWarnings("all")
public final class VineryCategory {

    @ConfigEntry(id = "barrel_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.vinery.barrel_recipes_disabled")
    @Comment(
            value = "Whether all Vinery fermentation barrel recipes are disabled",
            translation = "config.kaleidoscope_compat.vinery.barrel_recipes_disabled.comment"
    )
    public static boolean vineryBarrelRecipesDisabled = false;
}