package com.bmt.kaleidoscope_compat.config.category.arm;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class ArmConfig {

    @ConfigEntry(id = "pot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.pot_enabled")
    @Comment(
            value = "Whether Create mechanical arm pot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.pot_enabled.comment"
    )
    public static boolean potEnabled = true;

    @ConfigEntry(id = "stockpot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.stockpot_enabled")
    @Comment(
            value = "Whether Create mechanical arm stockpot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.stockpot_enabled.comment"
    )
    public static boolean stockpotEnabled = true;

    @ConfigEntry(id = "steamer_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.steamer_enabled")
    @Comment(
            value = "Whether Create mechanical arm steamer compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.steamer_enabled.comment"
    )
    public static boolean steamerEnabled = true;

    @ConfigEntry(id = "millstone_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.millstone_enabled")
    @Comment(
            value = "Whether Create mechanical arm millstone compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.millstone_enabled.comment"
    )
    public static boolean millstoneEnabled = true;

    @ConfigEntry(id = "shawarma_spit_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.shawarma_spit_enabled")
    @Comment(
            value = "Whether Create mechanical arm shawarma spit compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.shawarma_spit_enabled.comment"
    )
    public static boolean shawarmaSpitEnabled = true;

    @ConfigEntry(id = "teapot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm.teapot_enabled")
    @Comment(
            value = "Whether Create mechanical arm teapot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm.teapot_enabled.comment"
    )
    public static boolean teapotEnabled = true;
}