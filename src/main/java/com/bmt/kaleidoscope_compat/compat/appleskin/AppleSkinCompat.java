package com.bmt.kaleidoscope_compat.compat.appleskin;

import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

public class AppleSkinCompat {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        updateLunchBagFoodProperties(player, player.getItemInHand(InteractionHand.MAIN_HAND));
        updateLunchBagFoodProperties(player, player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static void updateLunchBagFoodProperties(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof TransmutationLunchBagItem)) {
            return;
        }

        Optional<FoodProperties> previewFood = findPreviewFood(stack, player);
        if (previewFood.isEmpty()) {
            stack.remove(DataComponents.FOOD);
            return;
        }

        FoodProperties properties = previewFood.get();
        stack.set(DataComponents.FOOD, new FoodProperties(
                properties.nutrition(),
                properties.saturation(),
                properties.canAlwaysEat(),
                properties.eatSeconds(),
                Optional.empty(),
                properties.effects()
        ));
    }

    private static Optional<FoodProperties> findPreviewFood(ItemStack lunchBag, Player player) {
        if (!TransmutationLunchBagItem.hasItems(lunchBag)) {
            return Optional.empty();
        }

        ItemStackHandler items = TransmutationLunchBagItem.getItems(lunchBag);
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (stackInSlot.isEmpty() || stackInSlot.getItem() instanceof TransmutationLunchBagItem) {
                continue;
            }

            FoodProperties foodProperties = stackInSlot.getItem().getFoodProperties(stackInSlot, player);
            if (foodProperties != null) {
                return Optional.of(foodProperties);
            }

            // If the first consumable entry is a potion, the bag should not preview hunger.
            if (stackInSlot.has(DataComponents.POTION_CONTENTS)) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
