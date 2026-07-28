package com.bmt.kaleidoscope_compat.capability;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class PotItemHandler implements IItemHandler {
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
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || pot.getLevel() == null) {
            return stack;
        }

        if (stack.is(TagMod.OIL)) {
            if (!simulate) {
                pot.onPlaceOil(pot.getLevel(), null, stack);
            }
            return ItemStack.EMPTY;
        }

        if (pot.getStatus() != PotBlockEntity.PUT_INGREDIENT) {
            return stack;
        }

        if (pot.isEmpty()) {
            return stack;
        }

        ItemStack toInsert = stack.copy();
        if (!simulate) {
            pot.addIngredient(pot.getLevel(), null, toInsert);
        }

        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        return remaining;
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
        return true;
    }
}