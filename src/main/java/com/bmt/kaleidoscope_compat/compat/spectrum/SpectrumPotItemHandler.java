package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SpectrumPotItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumPotItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.POT_BE.get(),
                (blockEntity, context) -> new PotItemHandler(blockEntity)
        );
    }

    private static class PotItemHandler implements IItemHandler {
        private final PotBlockEntity pot;

        public PotItemHandler(PotBlockEntity pot) {
            this.pot = pot;
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

            int status = pot.getStatus();

            if (status == PotBlockEntity.FINISHED || status == PotBlockEntity.BURNT) {
                return stack;
            }

            if (stack.is(TagMod.INGREDIENT_BLOCKLIST)) {
                return stack;
            }
            boolean isOil = stack.is(ModItems.OIL.get());
            boolean isOilPot = stack.is(ModItems.OIL_POT.get());

            if (isOil || isOilPot) {
                if (pot.getBlockState().getValue(com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL)) {
                    return stack;
                }

                if (simulate) {
                    return ItemStack.EMPTY;
                }

                ItemStack stackToUse = stack.copyWithCount(1);
                if (pot.onPlaceOil(pot.getLevel(), null, stackToUse)) {
                    ItemStack remaining = stack.copy();
                    remaining.shrink(1);
                    return remaining;
                }
                return stack;
            }

            if (!pot.getBlockState().getValue(com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL)) {
                return stack;
            }

            if (status != PotBlockEntity.PUT_INGREDIENT) {
                return stack;
            }

            if (simulate) {
                var inputs = pot.getInputs();
                for (ItemStack input : inputs) {
                    if (input.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
                return stack;
            }

            if (pot.addIngredient(pot.getLevel(), null, stack.copyWithCount(1))) {
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
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != 0) {
                return false;
            }
            int status = pot.getStatus();

            if (status == PotBlockEntity.FINISHED || status == PotBlockEntity.BURNT) {
                return false;
            }

            if (stack.is(TagMod.INGREDIENT_BLOCKLIST)) {
                return false;
            }

            if (stack.is(ModItems.OIL.get())) {
                return !pot.getBlockState().getValue(com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL);
            }

            if (status != PotBlockEntity.PUT_INGREDIENT) {
                return false;
            }
            if (!pot.getBlockState().getValue(com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL)) {
                return false;
            }
            return pot.addIngredient(pot.getLevel(), null, stack.copyWithCount(1));
        }
    }
}