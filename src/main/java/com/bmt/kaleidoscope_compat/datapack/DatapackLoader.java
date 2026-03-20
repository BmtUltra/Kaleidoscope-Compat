package com.bmt.kaleidoscope_compat.datapack;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.KCConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class DatapackLoader {

    @SubscribeEvent
    public static void onDatapackLoad(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            String mainPackName = switch (KCConfig.datapackMode) {
                case COMPAT -> "compat";
                case UNITE -> "unite";
            };
            addDatapack(event, mainPackName);

            if (KCConfig.soupDatapackEnabled) {
                addDatapack(event, "soup");
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