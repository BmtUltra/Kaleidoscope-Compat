package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("youkaisfeasts")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.youkaisfeasts",
        descriptionTranslation = "config.kaleidoscope_compat.category.youkaisfeasts.description",
        icon = "bowl-food"
)
public final class YoukaisFeastsCategory {

    @ConfigEntry(id = "steaming_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.youkaisfeasts.steaming_recipes_disabled")
    @Comment("Whether all Youkai's Homecoming steaming recipes are disabled")
    public static boolean steamingRecipesDisabled = false;
}