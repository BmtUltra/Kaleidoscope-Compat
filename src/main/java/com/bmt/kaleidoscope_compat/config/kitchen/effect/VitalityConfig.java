package com.bmt.kaleidoscope_compat.config.kitchen.effect;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class VitalityConfig {
    @ConfigEntry(id = "blacklist", type = EntryType.STRING, translation = "config.kaleidoscope_compat.kitchen.effect.vitality.blacklist")
    @Comment(
            value = "List of entity IDs that cannot be affected by Vitality effect",
            translation = "config.kaleidoscope_compat.kitchen.effect.vitality.blacklist.comment"
    )
    public static String blacklist = "";
}