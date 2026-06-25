package com.bmt.kaleidoscope_compat.config;

import com.bmt.kaleidoscope_compat.config.category.*;
import com.bmt.kaleidoscope_compat.config.kitchen.item.LunchBagConfig;
import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.Config;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Config(value = "kaleidoscope_compat", categories = {
        KitchenCategory.class,
        FarmersDelightCategory.class,
        YoukaisFeastsCategory.class,
        FarmAndCharmCategory.class,
        VineryCategory.class,
        LittleMaidCategory.class,
        SpectrumCategory.class,
        CreateCategory.class,
        OtherCategory.class
})
@ConfigInfo(
        title = "Kaleidoscope Compat",
        titleTranslation = "config.kaleidoscope_compat.title",
        description = "Compatibility mod for Kaleidoscope series mods",
        descriptionTranslation = "config.kaleidoscope_compat.description",
        icon = "utensils-crossed",
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
                ),
                @ConfigInfo.Link(
                        value = "https://github.com/your-repo/kaleidoscope-compat",
                        icon = "wiki",
                        text = "Wiki",
                        textTranslation = "config.kaleidoscope_compat.links.wiki"
                )
        }
)
@ConfigInfo.Gradient(value = "45deg", first = "#ff6b35", second = "#f7c59f")
@SuppressWarnings("all")
public final class MainConfig {
    @ConfigEntry(
            id = "datapack_mode",
            type = EntryType.ENUM,
            translation = "config.kaleidoscope_compat.datapack.mode"
    )
    @Comment(
            value = "NONE: Disable all datapacks except soup\nCOMPAT: Extensive compatibility with other mod items\nUNITE: Duplicate items of the unified module",
            translation = "config.kaleidoscope_compat.datapack.mode.comment"
    )
    public static DatapackMode datapackMode = DatapackMode.COMPAT;

    @ConfigEntry(id = "soup_datapack_enabled", type = EntryType.BOOLEAN, translation = "config.kaleidoscope_compat.datapack.soup_enabled")
    @Comment(
            value = "Enable soup base material",
            translation = "config.kaleidoscope_compat.datapack.soup_enabled.comment"
    )
    public static boolean soupDatapackEnabled = true;

    public static boolean isItemBlacklisted(Item item) {
        if (!LunchBagConfig.blacklistEnabled ||
                LunchBagConfig.blacklist == null ||
                LunchBagConfig.blacklist.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemIdStr = itemId.toString();
        for (String id : LunchBagConfig.blacklist.split(",")) {
            if (id.trim().equals(itemIdStr)) {
                return true;
            }
        }
        return false;
    }
}