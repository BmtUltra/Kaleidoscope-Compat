package com.bmt.kaleidoscope_compat.datapack;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.ForgeConfig;
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
            DatapackMode datapackMode = DatapackMode.valueOf(ForgeConfig.DATAPACK_MODE.get());
            boolean soupEnabled = ForgeConfig.SOUP_DATAPACK_ENABLED.get();

            if (datapackMode == DatapackMode.NONE) {
                if (soupEnabled) {
                    addDatapack(event, "soup");
                }
                return;
            }
            addDatapack(event, "always");

            String mainPackName = switch (datapackMode) {
                case COMPAT -> "compat";
                case UNITE -> "unite";
                default -> throw new IllegalStateException("Unexpected value: " + datapackMode);
            };
            addDatapack(event, mainPackName);

            if (soupEnabled) {
                addDatapack(event, "soup");
            }

            if (datapackMode == DatapackMode.UNITE) {
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