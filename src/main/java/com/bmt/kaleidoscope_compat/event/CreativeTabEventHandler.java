package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.init.KCItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabEventHandler {

    @SubscribeEvent
    public static void addJuiceBucketsToTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tavernMainTab = ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "tavern_main");
        if (event.getTabKey().location().equals(tavernMainTab)) {
            var entries = event.getEntries();
            ItemStack anchorItem = null;

            for (var entry : entries) {
                if (entry.getKey().is(ModItems.GLOW_BERRIES_BUCKET.get())) {
                    anchorItem = entry.getKey();
                    break;
                }
            }

            if (anchorItem != null) {
                entries.putAfter(anchorItem,
                        KCItems.WHITE_GRAPE_JUICE_BUCKET.get().getDefaultInstance(),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                entries.putAfter(KCItems.WHITE_GRAPE_JUICE_BUCKET.get().getDefaultInstance(),
                        KCItems.APPLE_JUICE_BUCKET.get().getDefaultInstance(),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}