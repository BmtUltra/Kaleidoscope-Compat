package com.bmt.kaleidoscope_compat.datapack;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

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
            addDatapack(event, datapackMode.name().toLowerCase());

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
//                if (ModList.get().isLoaded("youkaisfeasts")) {
//                    addDatapack(event, "unite_youkaisfeasts");
//                }
//                if (ModList.get().isLoaded("culturaldelights")) {
//                    addDatapack(event, "unite_culturaldelights");
//                }
//                if (ModList.get().isLoaded("bakeries")) {
//                    addDatapack(event, "unite_bakeries");
//                }
//                if (ModList.get().isLoaded("vinery")) {
//                    addDatapack(event, "unite_vinery");
//                }
            }
        }
    }

    private static void addDatapack(AddPackFindersEvent event, String packName) {
        Path resourcePath = ModList.get().getModFileById(KaleidoscopeCompat.MOD_ID).getFile().findResource("packs/" + packName);
        Pack pack = Pack.readMetaAndCreate(
                "kaleidoscope_compat:" + packName,
                Component.literal("Kaleidoscope Compat - " + packName.toUpperCase()),
                true,
                (path) -> new PathPackResources(path, resourcePath, false),
                PackType.SERVER_DATA,
                Pack.Position.TOP,
                PackSource.WORLD
        );
        if (pack != null) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }
}