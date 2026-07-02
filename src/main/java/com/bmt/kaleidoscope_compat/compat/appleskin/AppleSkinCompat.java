package com.bmt.kaleidoscope_compat.compat.appleskin;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

public final class AppleSkinCompat {
    private AppleSkinCompat() {
    }

    public static boolean isEnabled() {
        return ForgeConfig.LUNCH_BAG_APPLESKIN_COMPAT_ENABLED.get();
    }

    public static boolean shouldTreatAsFood(ItemStack stack, Player player) {
        if (isEnabled()) return false;
        return findPreviewFoodProperties(stack, player).isPresent();
    }

    public static Optional<FoodProperties> findPreviewFoodProperties(ItemStack lunchBag, Player player) {
        if (isEnabled()) return Optional.empty();
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

            if (stackInSlot.is(Items.POTION) || stackInSlot.is(Items.LINGERING_POTION) || stackInSlot.is(Items.SPLASH_POTION)) {
                if (!PotionUtils.getMobEffects(stackInSlot).isEmpty()) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}