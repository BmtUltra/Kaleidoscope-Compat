package com.bmt.kaleidoscope_compat.config.category;

import com.bmt.kaleidoscope_compat.config.kitchen.*;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;

@Category("kitchen")
@ConfigInfo(
        titleTranslation = "config.kaleidoscope_compat.category.kitchen",
        descriptionTranslation = "config.kaleidoscope_compat.category.kitchen.description",
        icon = "utensils"
)
@SuppressWarnings("all")
public final class KitchenCategory {

    @ConfigEntry(id = "millstone", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.millstone")
    public static final MillstoneConfig millstone = new MillstoneConfig();

    @ConfigEntry(id = "lunch_bag", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.lunch_bag")
    public static final LunchBagConfig lunchBag = new LunchBagConfig();

    @ConfigEntry(id = "scarecrow", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.scarecrow")
    public static final ScarecrowConfig scarecrow = new ScarecrowConfig();

    @ConfigEntry(id = "effect", type = EntryType.OBJECT, translation = "config.kaleidoscope_compat.kitchen.effect")
    public static final EffectConfig effect = new EffectConfig();
}