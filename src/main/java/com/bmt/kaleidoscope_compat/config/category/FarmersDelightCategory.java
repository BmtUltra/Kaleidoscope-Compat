package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("farmersdelight")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.farmersdelight",
        descriptionTranslation = "config.kaleidoscope_compat.category.farmersdelight.description",
        icon = "wheat",
links = {
@ConfigInfo.Link(
        value = "https://www.curseforge.com/minecraft/mc-mods/farmers-delight",
        icon = "curseforge",
        text = "CurseForge",
        textTranslation = "config.kaleidoscope_compat.links.curseforge"
),
@ConfigInfo.Link(
        value = "https://modrinth.com/mod/farmers-delight",
        icon = "modrinth",
        text = "Modrinth",
        textTranslation = "config.kaleidoscope_compat.links.modrinth"
),
@ConfigInfo.Link(
        value = "https://github.com/vectorwing/FarmersDelight",
        icon = "clipboard_list",
        text = "GitHub",
        textTranslation = "config.kaleidoscope_compat.links.github"
)
        }
)
public final class FarmersDelightCategory {

    @ConfigEntry(id = "cooking_pot_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farmersdelight.cooking_pot_recipes_disabled")
    @Comment("Whether all FarmersDelight cooking pot recipes are disabled")
    public static boolean cookingPotRecipesDisabled = false;

    @ConfigEntry(id = "cooking_pot_gui_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farmersdelight.cooking_pot_gui_disabled")
    @Comment("Whether the FarmersDelight cooking pot GUI is disabled")
    public static boolean cookingPotGuiDisabled = false;

    @ConfigEntry(id = "cutting_board_recipes_disabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farmersdelight.cutting_board_recipes_disabled")
    @Comment("Whether all FarmersDelight cutting board recipes are disabled")
    public static boolean cuttingBoardRecipesDisabled = false;

    @ConfigEntry(id = "rich_soil_hoe_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.farmersdelight.hoe_enabled")
    @Comment("Whether rich soil hoe tilling is enabled")
    public static boolean richSoilHoeEnabled = true;
}