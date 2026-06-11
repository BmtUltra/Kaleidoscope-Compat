package com.bmt.kaleidoscope_compat.config.kitchen.effect;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class ProjectileDodgeConfig {
    @ConfigEntry(id = "teleport_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.teleport_enabled")
    @Comment(
            value = "Whether projectile dodge teleport is enabled",
            translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.teleport_enabled.comment"
    )
    public static boolean teleportEnabled = true;

    @ConfigEntry(id = "duration_cost", type = EntryType.INTEGER, translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.duration_cost")
    @Comment(
            value = "Duration cost per dodge (in ticks)",
            translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.duration_cost.comment"
    )
    public static int durationCost = 200;
}