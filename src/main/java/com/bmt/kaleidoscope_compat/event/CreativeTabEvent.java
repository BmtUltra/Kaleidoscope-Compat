package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.init.KCItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class CreativeTabEvent {

    @SubscribeEvent
    public static void addJuiceBucketsToTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tavernMainTab = ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "tavern_main");
        if (event.getTabKey().location().equals(tavernMainTab)) {
            ItemStack glowBerriesBucket = ModItems.GLOW_BERRIES_BUCKET.get().getDefaultInstance();

            event.insertAfter(glowBerriesBucket,
                    KCItems.APPLE_JUICE_BUCKET.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}