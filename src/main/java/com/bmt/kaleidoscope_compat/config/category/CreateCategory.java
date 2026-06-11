package com.bmt.kaleidoscope_compat.config.category;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("create")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.create",
        descriptionTranslation = "config.kaleidoscope_compat.category.create.description",
        icon = "cog"
)
public final class CreateCategory {

    @ConfigEntry(id = "compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.compat_enabled")
    @Comment(
            value = "Whether Create mod compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.compat_enabled.comment"
    )
    public static boolean createCompatEnabled = true;

    @ConfigEntry(id = "arm_pot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_pot_enabled")
    @Comment(
            value = "Whether Create mechanical arm pot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_pot_enabled.comment"
    )
    public static boolean createArmPotEnabled = true;

    @ConfigEntry(id = "arm_stockpot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_stockpot_enabled")
    @Comment(
            value = "Whether Create mechanical arm stockpot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_stockpot_enabled.comment"
    )
    public static boolean createArmStockpotEnabled = true;

    @ConfigEntry(id = "arm_steamer_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_steamer_enabled")
    @Comment(
            value = "Whether Create mechanical arm steamer compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_steamer_enabled.comment"
    )
    public static boolean createArmSteamerEnabled = true;

    @ConfigEntry(id = "arm_millstone_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_millstone_enabled")
    @Comment(
            value = "Whether Create mechanical arm millstone compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_millstone_enabled.comment"
    )
    public static boolean createArmMillstoneEnabled = true;

    @ConfigEntry(id = "arm_shawarma_spit_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_shawarma_spit_enabled")
    @Comment(
            value = "Whether Create mechanical arm shawarma spit compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_shawarma_spit_enabled.comment"
    )
    public static boolean createArmShawarmaSpitEnabled = true;

    @ConfigEntry(id = "arm_teapot_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.create.arm_teapot_enabled")
    @Comment(
            value = "Whether Create mechanical arm teapot compatibility is enabled",
            translation = "config.kaleidoscope_compat.create.arm_teapot_enabled.comment"
    )
    public static boolean createArmTeapotEnabled = true;
}