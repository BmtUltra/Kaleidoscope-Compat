package com.bmt.kaleidoscope_compat.datapack;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.bmt.kaleidoscope_compat.util.DatapackMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DatapackLoader {

    @SubscribeEvent
    public static void onDatapackLoad(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            String mainPackName = switch (MainConfig.datapackMode) {
                case COMPAT -> "compat";
                case UNITE -> "unite";
            };
            addDatapack(event, mainPackName);

            if (MainConfig.soupDatapackEnabled) {
                addDatapack(event, "soup");
            }

            if (MainConfig.datapackMode == DatapackMode.UNITE) {
                if (ModList.get().isLoaded("farm_and_charm")) {
                    addDatapack(event, "unite_farm_and_charm");
                }
                if (ModList.get().isLoaded("farmersdelight")) {
                    addDatapack(event, "unite_farmersdelight");
                }
            }
        }
    }

    private static void addDatapack(AddPackFindersEvent event, String packName) {
        event.addRepositorySource((packConsumer) -> {
            Pack pack = Pack.readMetaAndCreate(
                    String.valueOf(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "packs/" + packName)),
                    Component.literal("Kaleidoscope Compat - " + packName.toUpperCase()),
                    true,
                    (path) -> (net.minecraft.server.packs.PackResources) KaleidoscopeCompat.class.getResourceAsStream("/data/" + KaleidoscopeCompat.MOD_ID + "/" + packName + ".zip"),
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    PackSource.WORLD
            );
            if (pack != null) {
                packConsumer.accept(pack);
            }
        });
    }
}