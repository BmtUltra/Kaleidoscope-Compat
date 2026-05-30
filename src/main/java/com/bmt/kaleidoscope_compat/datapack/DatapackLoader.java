package com.bmt.kaleidoscope_compat.datapack;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class DatapackLoader {

    @SubscribeEvent
    public static void onDatapackLoad(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            if (MainConfig.datapackModeValue == DatapackMode.NONE) {
                if (MainConfig.soupDatapackEnabledValue) {
                    addDatapack(event, "soup");
                }
                return;
            }
            addDatapack(event, "always");

            String mainPackName = switch (MainConfig.datapackModeValue) {
                case COMPAT -> "compat";
                case UNITE -> "unite";
                default -> throw new IllegalStateException("Unexpected value: " + MainConfig.datapackModeValue);
            };
            addDatapack(event, mainPackName);

            if (MainConfig.soupDatapackEnabledValue) {
                addDatapack(event, "soup");
            }

            if (MainConfig.cookingPotRecipesDisabledValue) {
                addDatapack(event, "disable_farmersdelight_cooking_pot");
            }
            if (MainConfig.cuttingBoardRecipesDisabledValue) {
                addDatapack(event, "disable_farmersdelight_cutting_board");
            }
            if (MainConfig.farmAndCharmCookingPotRecipesDisabledValue) {
                addDatapack(event, "disable_farm_and_charm_cooking_pot");
            }
            if (MainConfig.vineryBarrelRecipesDisabledValue) {
                addDatapack(event, "disable_vinery_fermentation_barrel");
            }
            if (MainConfig.steamingRecipesDisabledValue) {
                addDatapack(event, "disable_youkaisfeasts_steamer_pot");
            }

            if (MainConfig.datapackModeValue == DatapackMode.UNITE) {
                if (ModList.get().isLoaded("farm_and_charm")) {
                    addDatapack(event, "unite_farm_and_charm");
                }
                if (ModList.get().isLoaded("farmersdelight")) {
                    addDatapack(event, "unite_farmersdelight");
                }
                if (ModList.get().isLoaded("youkaisfeasts")) {
                    addDatapack(event, "unite_youkaisfeasts");
                }
                if (ModList.get().isLoaded("culturaldelights")) {
                    addDatapack(event, "unite_culturaldelights");
                }
                if (ModList.get().isLoaded("bakeries")) {
                    addDatapack(event, "unite_bakeries");
                }
                if (ModList.get().isLoaded("create")) {
                    addDatapack(event, "unite_create");
                }
                if (ModList.get().isLoaded("vinery")) {
                    addDatapack(event, "unite_vinery");
                }
            }
        }
    }

    private static void addDatapack(AddPackFindersEvent event, String packName) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "packs/" + packName),
                PackType.SERVER_DATA,
                Component.literal("Kaleidoscope Compat - " + packName.toUpperCase()),
                PackSource.WORLD,
                true,
                Pack.Position.TOP
        );
    }
}