package com.bmt.kaleidoscope_compat.compat.appleskin;

import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

public final class AppleSkinCompat {
    private AppleSkinCompat() {
    }

    public static boolean shouldTreatAsFood(ItemStack stack, Player player) {
        return findPreviewFoodProperties(stack, player).isPresent();
    }

    public static Optional<FoodProperties> findPreviewFoodProperties(ItemStack lunchBag, Player player) {
        if (!(lunchBag.getItem() instanceof TransmutationLunchBagItem) || !TransmutationLunchBagItem.hasItems(lunchBag)) {
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

            if (stackInSlot.has(DataComponents.POTION_CONTENTS)) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
