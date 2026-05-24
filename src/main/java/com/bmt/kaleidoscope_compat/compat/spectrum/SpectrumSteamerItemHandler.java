package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SpectrumSteamerItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumSteamerItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.STEAMER_BE.get(),
                (blockEntity, context) -> new SteamerItemHandler(blockEntity)
        );
    }

    private static class SteamerItemHandler implements IItemHandler {
        private final SteamerBlockEntity steamer;

        public SteamerItemHandler(SteamerBlockEntity steamer) {
            this.steamer = steamer;
        }

        @Override
        public int getSlots() {
            return 8;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < 8) {
                return steamer.getItems().get(slot);
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= 8 || stack.isEmpty()) {
                return stack;
            }

            boolean half = steamer.getBlockState().getValue(
                    com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HALF);
            int endIndex = half ? 4 : 8;
            if (slot >= endIndex) {
                return stack;
            }

            if (!steamer.getItems().get(slot).isEmpty()) {
                return stack;
            }

            if (simulate) {
                if (steamer.placeFood(steamer.getLevel(), null, stack.copyWithCount(1))) {
                    return ItemStack.EMPTY;
                }
                return stack;
            }

            if (steamer.placeFood(steamer.getLevel(), null, stack.copyWithCount(1))) {
                ItemStack remaining = stack.copy();
                remaining.shrink(1);
                return remaining;
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= 8) {
                return ItemStack.EMPTY;
            }

            ItemStack stack = steamer.getItems().get(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (steamer.getCookingTime()[slot] != -1) {
                return ItemStack.EMPTY;
            }

            int extractAmount = Math.min(amount, stack.getCount());
            ItemStack extracted = stack.copyWithCount(extractAmount);

            if (!simulate) {
                steamer.getItems().set(slot, ItemStack.EMPTY);
                steamer.getCookingProgress()[slot] = 0;
                steamer.getCookingTime()[slot] = 0;
                steamer.setChanged();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < 0 || slot >= 8) {
                return false;
            }

            boolean half = steamer.getBlockState().getValue(
                    com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HALF);
            int endIndex = half ? 4 : 8;
            if (slot >= endIndex) {
                return false;
            }

            if (!steamer.getItems().get(slot).isEmpty()) {
                return false;
            }
            return steamer.placeFood(steamer.getLevel(), null, stack.copyWithCount(1));
        }
    }
}