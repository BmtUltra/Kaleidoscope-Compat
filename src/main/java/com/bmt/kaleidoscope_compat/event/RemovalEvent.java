package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.util.MutableHashedLinkedMap;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RemovalEvent {

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = event.getEntries();
        entries.iterator().forEachRemaining(entry -> {
            ItemStack stack = entry.getKey();
            if (stack.is(TagUtil.Items.UNITED)) {
                entries.remove(entry.getKey());
            }
        });
    }
}