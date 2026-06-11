package com.bmt.kaleidoscope_compat.config.kitchen.entity;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class ScarecrowConfig {
    @ConfigEntry(id = "repel_phantoms", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.scarecrow.repel_phantoms")
    @Comment(
            value = "Whether scarecrows can repel phantoms",
            translation = "config.kaleidoscope_compat.kitchen.scarecrow.repel_phantoms.comment"
    )
    public static boolean repelPhantoms = true;
}