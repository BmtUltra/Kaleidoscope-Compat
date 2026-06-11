package com.bmt.kaleidoscope_compat.config.category;

import com.bmt.kaleidoscope_compat.config.kitchen.block.MillstoneConfig;
import com.bmt.kaleidoscope_compat.config.kitchen.effect.ProjectileDodgeConfig;
import com.bmt.kaleidoscope_compat.config.kitchen.effect.VitalityConfig;
import com.bmt.kaleidoscope_compat.config.kitchen.entity.ScarecrowConfig;
import com.bmt.kaleidoscope_compat.config.kitchen.item.LunchBagConfig;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("kitchen")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.kitchen",
        descriptionTranslation = "config.kaleidoscope_compat.category.kitchen.description",
        icon = "utensils",
links = {
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "curseforge",
        text = "CurseForge",
        textTranslation = "config.kaleidoscope_compat.links.curseforge"
),
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "modrinth",
        text = "Modrinth",
        textTranslation = "config.kaleidoscope_compat.links.modrinth"
),
@ConfigInfo.Link(
        value = "https://github.com/your-repo/kaleidoscope-compat",
        icon = "clipboard_list",
        text = "GitHub",
        textTranslation = "config.kaleidoscope_compat.links.github"
)
        }
)
@SuppressWarnings("all")
public final class KitchenCategory {

    @ConfigEntry(id = "block_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.kitchen.effect.block_enabled")
    @Comment(
            value = "Whether Nourishment effect is blocked when player has Satiated Shield",
            translation = "config.kaleidoscope_compat.kitchen.effect.block_enabled.comment"
    )
    public static boolean blockEnabled = true;

    @ConfigEntry(id = "millstone", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.millstone")
    @Comment(
            value = "Millstone related settings",
            translation = "config.kaleidoscope_compat.kitchen.millstone.comment"
    )
    public static final MillstoneConfig millstone = new MillstoneConfig();

    @ConfigEntry(id = "lunch_bag", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.lunch_bag")
    @Comment(
            value = "Transmutation Lunch Bag related settings",
            translation = "config.kaleidoscope_compat.kitchen.lunch_bag.comment"
    )
    public static final LunchBagConfig lunchBag = new LunchBagConfig();

    @ConfigEntry(id = "scarecrow", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.scarecrow")
    @Comment(
            value = "Scarecrow related settings",
            translation = "config.kaleidoscope_compat.kitchen.scarecrow.comment"
    )
    public static final ScarecrowConfig scarecrow = new ScarecrowConfig();

    @ConfigEntry(id = "projectile_dodge", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge")
    @Comment(
            value = "Projectile dodge related settings",
            translation = "config.kaleidoscope_compat.kitchen.effect.projectile_dodge.comment"
    )
    public static final ProjectileDodgeConfig projectileDodge = new ProjectileDodgeConfig();

    @ConfigEntry(id = "vitality", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.effect.vitality")
    @Comment(
            value = "Vitality effect related settings",
            translation = "config.kaleidoscope_compat.kitchen.effect.vitality.comment"
    )
    public static final VitalityConfig vitality = new VitalityConfig();
}