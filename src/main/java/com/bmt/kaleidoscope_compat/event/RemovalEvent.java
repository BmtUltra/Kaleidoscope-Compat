package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class RemovalEvent {

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        for (ItemStack entry : event.getParentEntries()) {
            if (entry != null && entry.is(TagUtil.Items.UNITED)) {
                event.remove(entry, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }

        for (ItemStack entry : event.getSearchEntries()) {
            if (entry != null && entry.is(TagUtil.Items.UNITED)) {
                event.remove(entry, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        }
    }
}