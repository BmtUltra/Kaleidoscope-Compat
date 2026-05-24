package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ChoppingBoardBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SpectrumChoppingBoardItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumChoppingBoardItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.CHOPPING_BOARD_BE.get(),
                (blockEntity, context) -> new ChoppingBoardItemHandler(blockEntity)
        );
    }

    private static class ChoppingBoardItemHandler implements IItemHandler {
        private final ChoppingBoardBlockEntity choppingBoard;

        public ChoppingBoardItemHandler(ChoppingBoardBlockEntity choppingBoard) {
            this.choppingBoard = choppingBoard;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty()) {
                return stack;
            }

            if (!choppingBoard.getCurrentCutStack().isEmpty()) {
                return stack;
            }

            if (simulate) {
                if (choppingBoard.onPutItem(choppingBoard.getLevel(), null, stack.copyWithCount(1))) {
                    return ItemStack.EMPTY;
                }
                return stack;
            }

            if (choppingBoard.onPutItem(choppingBoard.getLevel(), null, stack.copyWithCount(1))) {
                ItemStack remaining = stack.copy();
                remaining.shrink(1);
                return remaining;
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != 0) {
                return false;
            }

            if (!choppingBoard.getCurrentCutStack().isEmpty()) {
                return false;
            }
            return choppingBoard.onPutItem(choppingBoard.getLevel(), null, stack.copyWithCount(1));
        }
    }
}