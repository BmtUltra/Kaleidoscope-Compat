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
        icon = "wine-bottle"
)
public final class VineryCategory {

    @ConfigEntry(id = "barrel_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.vinery.barrel_recipes_disabled")
    @Comment("Whether all Vinery fermentation barrel recipes are disabled")
    public static boolean vineryBarrelRecipesDisabled = false;
}