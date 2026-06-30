package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class EntityLootEvent {

    private static final String REPLACEMENT_TYPE = "entityloot";

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        List<ItemEntity> drops = (List<ItemEntity>) event.getDrops();
        if (drops.isEmpty()) {
            return;
        }

        List<ItemEntity> newDrops = new ArrayList<>();
        boolean modified = false;

        for (ItemEntity itemEntity : drops) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) {
                newDrops.add(itemEntity);
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);

            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    ItemStack newStack = new ItemStack(newItem, stack.getCount());
                    ItemEntity newEntity = new ItemEntity(
                            itemEntity.level(),
                            itemEntity.getX(),
                            itemEntity.getY(),
                            itemEntity.getZ(),
                            newStack
                    );
                    newEntity.setPickUpDelay(10);
                    newDrops.add(newEntity);
                    modified = true;
                    itemEntity.discard();
                } else {
                    newDrops.add(itemEntity);
                }
            } else {
                newDrops.add(itemEntity);
            }
        }

        if (modified) {
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        }
    }
}