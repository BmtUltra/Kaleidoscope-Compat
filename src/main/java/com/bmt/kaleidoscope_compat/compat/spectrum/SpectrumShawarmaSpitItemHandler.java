package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SpectrumShawarmaSpitItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumShawarmaSpitItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.SHAWARMA_SPIT_BE.get(),
                (blockEntity, context) -> new ShawarmaSpitItemHandler(blockEntity)
        );
    }

    private static class ShawarmaSpitItemHandler implements IItemHandler {
        private final ShawarmaSpitBlockEntity spit;

        public ShawarmaSpitItemHandler(ShawarmaSpitBlockEntity spit) {
            this.spit = spit;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0) {
                return spit.cookingItem;
            } else if (slot == 1) {
                return spit.cookedItem;
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty()) {
                return stack;
            }

            if (!spit.cookedItem.isEmpty()) {
                return stack;
            }

            if (!spit.cookingItem.isEmpty()) {
                return stack;
            }

            if (simulate) {
                if (spit.onPutCookingItem(spit.getLevel(), stack.copyWithCount(1))) {
                    return ItemStack.EMPTY;
                }
                return stack;
            }

            if (spit.onPutCookingItem(spit.getLevel(), stack.copyWithCount(1))) {
                ItemStack remaining = stack.copy();
                remaining.shrink(1);
                return remaining;
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1) {
                return ItemStack.EMPTY;
            }

            if (spit.cookedItem.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (spit.cookTime > 0) {
                return ItemStack.EMPTY;
            }

            ItemStack result = spit.cookedItem.copy();
            int extractAmount = Math.min(amount, result.getCount());
            ItemStack extracted = result.copyWithCount(extractAmount);

            if (!simulate) {
                spit.cookingItem = ItemStack.EMPTY;
                spit.cookedItem = ItemStack.EMPTY;
                spit.cookTime = 0;
                spit.setChanged();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != 0) {
                return false;
            }

            if (!spit.cookedItem.isEmpty()) {
                return false;
            }

            if (!spit.cookingItem.isEmpty()) {
                return false;
            }
            return spit.onPutCookingItem(spit.getLevel(), stack.copyWithCount(1));
        }
    }
}