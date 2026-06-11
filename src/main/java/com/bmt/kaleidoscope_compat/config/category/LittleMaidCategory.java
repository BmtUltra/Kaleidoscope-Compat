package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("little_maid")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.little_maid",
        descriptionTranslation = "config.kaleidoscope_compat.category.little_maid.description",
        icon = "doll"
)
public final class LittleMaidCategory {

    @ConfigEntry(id = "compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.little_maid.compat_enabled")
    @Comment("Whether Little Maid mod compatibility is enabled")
    public static boolean littleMaidCompatEnabled = true;

    @ConfigEntry(id = "chopping_board_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.little_maid.chopping_board_enabled")
    @Comment("Whether Little Maid chopping board task is enabled")
    public static boolean littleMaidChoppingBoardEnabled = true;

    @ConfigEntry(id = "millstone_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.little_maid.millstone_enabled")
    @Comment("Whether Little Maid millstone task is enabled")
    public static boolean littleMaidMillstoneEnabled = true;

    @ConfigEntry(id = "pressing_tub_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.little_maid.pressing_tub_enabled")
    @Comment("Whether Little Maid pressing tub task is enabled")
    public static boolean littleMaidPressingTubEnabled = true;
}