package com.bmt.kaleidoscope_compat.config;

import com.bmt.kaleidoscope_compat.config.category.*;
import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import com.teamresourceful.resourcefulconfig.api.annotations.*;
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

    public static boolean millstoneStackingEnabled = KitchenCategory.millstone.stackingEnabled;
    public static boolean lunchBagBlacklistEnabled = KitchenCategory.lunchBag.blacklistEnabled;
    public static String lunchBagBlacklist = KitchenCategory.lunchBag.blacklist;
    public static boolean transmutationLunchBagBackEnabled = KitchenCategory.lunchBag.backBehaviorEnabled;
    public static boolean appleskinCompatEnabled = KitchenCategory.lunchBag.appleskinCompatEnabled;
    public static boolean scarecrowRepelPhantoms = KitchenCategory.scarecrow.repelPhantoms;
    public static boolean nourishmentEffectBlockEnabled = KitchenCategory.blockEnabled;
    public static boolean projectileDodgeTeleportEnabled = KitchenCategory.projectileDodge.teleportEnabled;
    public static int projectileDodgeDurationCost = KitchenCategory.projectileDodge.durationCost;
    public static String vitalityBlacklist = KitchenCategory.vitality.blacklist;
    public static boolean cookingPotRecipesDisabled = FarmersDelightCategory.cookingPotRecipesDisabled;
    public static boolean cookingPotGuiDisabled = FarmersDelightCategory.cookingPotGuiDisabled;
    public static boolean cuttingBoardRecipesDisabled = FarmersDelightCategory.cuttingBoardRecipesDisabled;
    public static boolean richSoilHoeEnabled = FarmersDelightCategory.richSoilHoeEnabled;
    public static boolean steamingRecipesDisabled = YoukaisFeastsCategory.steamingRecipesDisabled;
    public static boolean farmAndCharmCookingPotRecipesDisabled = FarmAndCharmCategory.farmAndCharmCookingPotRecipesDisabled;
    public static boolean farmAndCharmCompatEnabled = FarmAndCharmCategory.farmAndCharmCompatEnabled;
    public static boolean vineryBarrelRecipesDisabled = VineryCategory.vineryBarrelRecipesDisabled;
    public static boolean littleMaidCompatEnabled = LittleMaidCategory.littleMaidCompatEnabled;
    public static boolean littleMaidChoppingBoardEnabled = LittleMaidCategory.littleMaidChoppingBoardEnabled;
    public static boolean littleMaidMillstoneEnabled = LittleMaidCategory.littleMaidMillstoneEnabled;
    public static boolean littleMaidPressingTubEnabled = LittleMaidCategory.littleMaidPressingTubEnabled;
    public static boolean spectrumCompatEnabled = SpectrumCategory.spectrumCompatEnabled;
    public static boolean spectrumPotItemHandlerEnabled = SpectrumCategory.spectrumPotItemHandlerEnabled;
    public static boolean spectrumChoppingBoardItemHandlerEnabled = SpectrumCategory.spectrumChoppingBoardItemHandlerEnabled;
    public static boolean spectrumMillstoneItemHandlerEnabled = SpectrumCategory.spectrumMillstoneItemHandlerEnabled;
    public static boolean spectrumShawarmaSpitItemHandlerEnabled = SpectrumCategory.spectrumShawarmaSpitItemHandlerEnabled;
    public static boolean spectrumSteamerItemHandlerEnabled = SpectrumCategory.spectrumSteamerItemHandlerEnabled;
    public static boolean spectrumTeapotItemHandlerEnabled = SpectrumCategory.spectrumTeapotItemHandlerEnabled;
    public static boolean spectrumTrashCanItemHandlerEnabled = SpectrumCategory.spectrumTrashCanItemHandlerEnabled;
    public static boolean spectrumPastelNodeCompatEnabled = SpectrumCategory.spectrumPastelNodeCompatEnabled;
    public static boolean createCompatEnabled = CreateCategory.createCompatEnabled;
    public static boolean createArmPotEnabled = CreateCategory.createArmPotEnabled;
    public static boolean createArmStockpotEnabled = CreateCategory.createArmStockpotEnabled;
    public static boolean createArmSteamerEnabled = CreateCategory.createArmSteamerEnabled;
    public static boolean createArmMillstoneEnabled = CreateCategory.createArmMillstoneEnabled;
    public static boolean createArmShawarmaSpitEnabled = CreateCategory.createArmShawarmaSpitEnabled;
    public static boolean createArmTeapotEnabled = CreateCategory.createArmTeapotEnabled;
    public static boolean jeiCompatEnabled = OtherCategory.jeiCompatEnabled;
    public static boolean thirstCompatEnabled = OtherCategory.thirstCompatEnabled;
    public static boolean quarkSickleHarvestFixEnabled = OtherCategory.quarkSickleHarvestFixEnabled;
    public static boolean suppressTagLoadErrors = OtherCategory.suppressTagLoadErrors;

    public static boolean isItemBlacklisted(Item item) {
        if (!KitchenCategory.lunchBag.blacklistEnabled ||
                KitchenCategory.lunchBag.blacklist == null ||
                KitchenCategory.lunchBag.blacklist.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) return false;
        String itemIdStr = itemId.toString();
        for (String id : KitchenCategory.lunchBag.blacklist.split(",")) {
            if (id.trim().equals(itemIdStr)) {
                return true;
            }
        }
        return false;
    }
}