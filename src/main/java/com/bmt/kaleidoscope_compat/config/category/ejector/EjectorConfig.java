package com.bmt.kaleidoscope_compat.config.category.ejector;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class EjectorConfig {
    @ConfigEntry(id = "pot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.pot_enabled")
    @Comment(value = "Whether weighted ejectors interact with woks", translation = "config.kaleidoscope_compat.create.ejector.pot_enabled.comment")
    public static boolean potEnabled = true;

    @ConfigEntry(id = "stockpot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.stockpot_enabled")
    @Comment(value = "Whether weighted ejectors interact with stockpots", translation = "config.kaleidoscope_compat.create.ejector.stockpot_enabled.comment")
    public static boolean stockpotEnabled = true;

    @ConfigEntry(id = "steamer_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.steamer_enabled")
    @Comment(value = "Whether weighted ejectors interact with steamers", translation = "config.kaleidoscope_compat.create.ejector.steamer_enabled.comment")
    public static boolean steamerEnabled = true;

    @ConfigEntry(id = "millstone_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.millstone_enabled")
    @Comment(value = "Whether weighted ejectors interact with millstones", translation = "config.kaleidoscope_compat.create.ejector.millstone_enabled.comment")
    public static boolean millstoneEnabled = true;

    @ConfigEntry(id = "shawarma_spit_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.shawarma_spit_enabled")
    @Comment(value = "Whether weighted ejectors interact with shawarma spits", translation = "config.kaleidoscope_compat.create.ejector.shawarma_spit_enabled.comment")
    public static boolean shawarmaSpitEnabled = true;

    @ConfigEntry(id = "teapot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.ejector.teapot_enabled")
    @Comment(value = "Whether weighted ejectors interact with teapots", translation = "config.kaleidoscope_compat.create.ejector.teapot_enabled.comment")
    public static boolean teapotEnabled = true;
}
