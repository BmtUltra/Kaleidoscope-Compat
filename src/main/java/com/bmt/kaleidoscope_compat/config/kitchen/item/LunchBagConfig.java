package com.bmt.kaleidoscope_compat.config.kitchen.item;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@ConfigObject
@SuppressWarnings("all")
public final class LunchBagConfig {
    @ConfigEntry(id = "blacklist_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist_enabled")
    @Comment(
            value = "Whether the lunch bag blacklist is enabled",
            translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist_enabled.comment"
    )
    public static boolean blacklistEnabled = true;

    @ConfigEntry(id = "blacklist", type = EntryType.STRING, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist")
    @Comment(
            value = "List of item IDs that cannot be put into the Transmutation Lunch Bag",
            translation = "config.kaleidoscope_compat.kitchen.lunch_bag.blacklist.comment"
    )
    public static String blacklist = "artifacts:eternal_steak,kaleidoscope_nether:everlasting_flame_steak";

    @ConfigEntry(id = "back_behavior_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.back_behavior_enabled")
    @Comment(
            value = "Whether the modified Transmutation Lunch Bag behavior is enabled",
            translation = "config.kaleidoscope_compat.kitchen.lunch_bag.back_behavior_enabled.comment"
    )
    public static boolean backBehaviorEnabled = false;

    @ConfigEntry(id = "appleskin_compat_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.lunch_bag.appleskin_compat_enabled")
    @Comment(
            value = "Whether AppleSkin mod compatibility is enabled",
            translation = "config.kaleidoscope_compat.kitchen.lunch_bag.appleskin_compat_enabled.comment"
    )
    public static boolean appleskinCompatEnabled = true;
}