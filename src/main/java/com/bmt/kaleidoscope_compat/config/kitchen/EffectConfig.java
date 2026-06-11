package com.bmt.kaleidoscope_compat.config.kitchen;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
public final class EffectConfig {
    @ConfigEntry(id = "block_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.effect.block_enabled")
    @Comment("Whether Nourishment effect is blocked when player has Satiated Shield")
    public static boolean blockEnabled = true;

    @ConfigEntry(id = "projectile_dodge_teleport_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.teleport_enabled")
    @Comment("Whether projectile dodge teleport is enabled")
    public static boolean projectileDodgeTeleportEnabled = false;

    @ConfigEntry(id = "projectile_dodge_duration_cost", type = EntryType.INTEGER, translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.duration_cost")
    @Comment("Duration cost per dodge (in ticks)")
    public static int projectileDodgeDurationCost = 200;

    @ConfigEntry(id = "vitality_blacklist", type = EntryType.STRING, translation = "config.kaleidoscope_compat.kitchen.effect.vitality.blacklist")
    @Comment("List of entity IDs that cannot be affected by Vitality effect")
    public static String vitalityBlacklist = "";
}