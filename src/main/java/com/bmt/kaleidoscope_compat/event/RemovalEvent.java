package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RemovalEvent {

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        List<ItemStack> toRemove = new ArrayList<>();

        for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : event.getEntries()) {
            ItemStack stack = entry.getKey();
            if (stack != null && stack.is(TagUtil.Items.UNITED)) {
                toRemove.add(stack);
            }
        }

        for (ItemStack stack : toRemove) {
            event.getEntries().remove(stack);
        }
    }
}