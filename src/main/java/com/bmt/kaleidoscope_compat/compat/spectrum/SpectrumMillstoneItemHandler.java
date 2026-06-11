package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SpectrumMillstoneItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumMillstoneItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.MILLSTONE_BE.get(),
                (blockEntity, context) -> new MillstoneItemHandler(blockEntity)
        );
    }

    private static class MillstoneItemHandler implements IItemHandler {
        private final MillstoneBlockEntity millstone;

        public MillstoneItemHandler(MillstoneBlockEntity millstone) {
            this.millstone = millstone;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0) {
                return millstone.getInput();
            } else if (slot == 1) {
                for (int i = 0; i < millstone.getOutputs().getSlots(); i++) {
                    ItemStack stack = millstone.getOutputs().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        return stack;
                    }
                }
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty()) {
                return stack;
            }

            if (!millstone.isOutputEmpty()) {
                return stack;
            }

            int amountToInsert = Math.min(stack.getCount(), MillstoneBlockEntity.MAX_INPUT_COUNT);
            ItemStack stackToInsert = stack.copyWithCount(amountToInsert);

            if (simulate) {
                if (millstone.onPutItem(millstone.getLevel(), stackToInsert)) {
                    ItemStack remaining = stack.copy();
                    remaining.shrink(amountToInsert);
                    return remaining;
                }
                return stack;
            }

            if (millstone.onPutItem(millstone.getLevel(), stackToInsert)) {
                ItemStack remaining = stack.copy();
                remaining.shrink(amountToInsert);
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

            ItemStack output = ItemStack.EMPTY;
            int outputSlot = -1;
            for (int i = 0; i < millstone.getOutputs().getSlots(); i++) {
                ItemStack stack = millstone.getOutputs().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    output = stack;
                    outputSlot = i;
                    break;
                }
            }

            if (output.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int extractAmount = Math.min(amount, output.getCount());
            ItemStack extracted = output.copyWithCount(extractAmount);

            if (!simulate) {
                output.shrink(extractAmount);
                if (output.isEmpty()) {
                    millstone.getOutputs().extractItem(outputSlot, 1, false);
                }
                if (millstone.isOutputEmpty()) {
                    millstone.resetWhenTakeout();
                }
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return MillstoneBlockEntity.MAX_INPUT_COUNT;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != 0) {
                return false;
            }
            return millstone.onPutItem(millstone.getLevel(), stack.copyWithCount(1));
        }
    }
}