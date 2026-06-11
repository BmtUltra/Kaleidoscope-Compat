package com.bmt.kaleidoscope_compat.config.kitchen;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
public final class MillstoneConfig {
    @ConfigEntry(id = "stacking_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.millstone.stacking_enabled")
    @Comment("Whether millstone item stacking is enabled")
    public static boolean stackingEnabled = true;
}