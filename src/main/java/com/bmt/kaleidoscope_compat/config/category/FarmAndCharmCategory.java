package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("farm_and_charm")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.farm_and_charm",
        descriptionTranslation = "config.kaleidoscope_compat.category.farm_and_charm.description",
        icon = "shovel"
)
public final class FarmAndCharmCategory {

    @ConfigEntry(id = "cooking_pot_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farm_and_charm.cooking_pot_recipes_disabled")
    @Comment("Whether all Farm and Charm cooking pot recipes are disabled")
    public static boolean farmAndCharmCookingPotRecipesDisabled = false;

    @ConfigEntry(id = "farm_and_charm_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farm_and_charm.compat_enabled")
    @Comment("Whether Farm and Charm mod compatibility is enabled")
    public static boolean farmAndCharmCompatEnabled = true;
}