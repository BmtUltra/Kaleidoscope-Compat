package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class DrinkEventHandler {

    private static final ResourceLocation TAVERN_DRINK_BE = Objects.requireNonNull(ResourceLocation.tryBuild("kaleidoscope_tavern", "drink"));

    @SubscribeEvent
    public static void onBlockEntityTypeAddBlocks(BlockEntityTypeAddBlocksEvent event) {
        if (!ModList.get().isLoaded("kaleidoscope_tavern")) {
            return;
        }

        BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(TAVERN_DRINK_BE).ifPresent(drinkType -> {
            List<Item> drinkItems = BuiltInRegistries.ITEM.getTag(TagUtil.Items.DRINKS)
                    .map(tag -> tag.stream()
                            .map(Holder::value)
                            .toList())
                    .orElse(List.of());
            for (Item drinkItem : drinkItems) {
                event.modify(drinkType, Block.byItem(drinkItem));
            }
        });
    }
}