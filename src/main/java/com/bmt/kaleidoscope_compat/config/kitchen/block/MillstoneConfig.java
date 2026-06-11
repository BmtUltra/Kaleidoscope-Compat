package com.bmt.kaleidoscope_compat.config.kitchen.block;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class MillstoneConfig {
    @ConfigEntry(id = "stacking_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.millstone.stacking_enabled")
    @Comment(
            value = "Whether millstone item stacking is enabled",
            translation = "config.kaleidoscope_compat.kitchen.millstone.stacking_enabled.comment"
    )
    public static boolean stackingEnabled = false;
}