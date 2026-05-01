package com.bmt.kaleidoscope_compat.compat.appleskin;

import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AppleSkinCompat {
    private static final Map<UUID, ItemStack> lastHeldLunchBag = new HashMap<>();
    private static final Map<UUID, Boolean> lastLunchBagHasItems = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ModList.get().isLoaded("appleskin")) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mainHandItem = player.getMainHandItem();
            UUID playerId = player.getUUID();

            boolean isLunchBag = mainHandItem.getItem() instanceof TransmutationLunchBagItem;

            ItemStack lastBag = lastHeldLunchBag.get(playerId);
            boolean lastHadItems = lastLunchBagHasItems.getOrDefault(playerId, false);

            boolean hasItems = isLunchBag && TransmutationLunchBagItem.hasItems(mainHandItem);

            boolean shouldUpdate = false;

            if (isLunchBag) {
                if (lastBag == null || !ItemStack.matches(mainHandItem, lastBag)) {
                    shouldUpdate = true;
                }

                else if (hasItems != lastHadItems) {
                    shouldUpdate = true;
                }

                else if (hasItems) {
                    ItemStackHandler currentItems = TransmutationLunchBagItem.getItems(mainHandItem);
                    ItemStackHandler lastItems = TransmutationLunchBagItem.getItems(lastBag);

                    if (!areItemHandlersEqual(currentItems, lastItems)) {
                        shouldUpdate = true;
                    }
                }
            } else if (lastBag != null) {
                shouldUpdate = true;
            }

            if (shouldUpdate) {
                updateLunchBagFoodProperties(player, mainHandItem, isLunchBag, hasItems);

                if (isLunchBag) {
                    lastHeldLunchBag.put(playerId, mainHandItem.copy());
                    lastLunchBagHasItems.put(playerId, hasItems);
                } else {
                    lastHeldLunchBag.remove(playerId);
                    lastLunchBagHasItems.remove(playerId);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID playerId = player.getUUID();
            lastHeldLunchBag.remove(playerId);
            lastLunchBagHasItems.remove(playerId);
        }
    }

    private static void updateLunchBagFoodProperties(ServerPlayer player, ItemStack lunchBag, boolean isLunchBag, boolean hasItems) {
        if (!isLunchBag) {
            return;
        }

        if (!hasItems) {
            lunchBag.remove(DataComponents.FOOD);
            return;
        }

        ItemStackHandler items = TransmutationLunchBagItem.getItems(lunchBag);

        ItemStack firstFood = ItemStack.EMPTY;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack foodStack = items.getStackInSlot(i);
            if (!foodStack.isEmpty()) {
                if (foodStack.getItem() instanceof TransmutationLunchBagItem) {
                    continue;
                }

                FoodProperties foodProperties = foodStack.getItem().getFoodProperties(foodStack, player);
                if (foodProperties != null) {
                    firstFood = foodStack;
                    break;
                }
            }
        }

        if (!firstFood.isEmpty()) {
            FoodProperties firstFoodProperties = firstFood.getItem().getFoodProperties(firstFood, player);
            if (firstFoodProperties != null) {
                FoodProperties.Builder builder = new FoodProperties.Builder();
                builder.nutrition(firstFoodProperties.nutrition());
                builder.saturationModifier(firstFoodProperties.saturation());

                lunchBag.set(DataComponents.FOOD, builder.build());
            } else {
                lunchBag.remove(DataComponents.FOOD);
            }
        } else {
            lunchBag.remove(DataComponents.FOOD);
        }
    }

    private static boolean areItemHandlersEqual(ItemStackHandler handler1, ItemStackHandler handler2) {
        if (handler1.getSlots() != handler2.getSlots()) {
            return false;
        }

        for (int i = 0; i < handler1.getSlots(); i++) {
            ItemStack stack1 = handler1.getStackInSlot(i);
            ItemStack stack2 = handler2.getStackInSlot(i);

            if (!ItemStack.matches(stack1, stack2)) {
                return false;
            }
        }
        return true;
    }
}