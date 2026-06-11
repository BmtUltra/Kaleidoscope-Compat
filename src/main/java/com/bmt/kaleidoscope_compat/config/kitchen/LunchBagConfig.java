package com.bmt.kaleidoscope_compat.config.kitchen;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
public final class LunchBagConfig {
    @ConfigEntry(id = "blacklist_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist_enabled")
    @Comment("Whether the lunch bag blacklist is enabled")
    public static boolean blacklistEnabled = true;

    @ConfigEntry(id = "blacklist", type = EntryType.STRING, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist")
    @Comment("List of item IDs that cannot be put into the Transmutation Lunch Bag")
    public static String blacklist = "artifacts:eternal_steak,kaleidoscope_nether:everlasting_flame_steak";

    @ConfigEntry(id = "back_behavior_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.back_behavior_enabled")
    @Comment("Whether the modified Transmutation Lunch Bag behavior is enabled")
    public static boolean backBehaviorEnabled = false;

    @ConfigEntry(id = "appleskin_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.appleskin_compat_enabled")
    @Comment("Whether AppleSkin mod compatibility is enabled")
    public static boolean appleskinCompatEnabled = true;
}