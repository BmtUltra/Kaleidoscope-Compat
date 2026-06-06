package com.bmt.kaleidoscope_compat.compat.spectrum;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.TeapotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class SpectrumTeapotItemHandler {
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(SpectrumTeapotItemHandler::register);
        initialized = true;
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.TEAPOT_BE.get(),
                (blockEntity, context) -> new TeapotItemHandler(blockEntity)
        );
    }

    private static class TeapotItemHandler implements IItemHandler {
        private final TeapotBlockEntity teapot;
        private final TeapotBlockEntityAccessor accessor;

        public TeapotItemHandler(TeapotBlockEntity teapot) {
            this.teapot = teapot;
            this.accessor = (TeapotBlockEntityAccessor) teapot;
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
            int status = teapot.getStatus();

            if (status != ITeapot.PUT_INGREDIENT) {
                return stack;
            }

            var fluidCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidCap == null) {
                return stack;
            }

            FluidStack fluidInTank = fluidCap.getFluidInTank(0);
            if (fluidInTank.isEmpty() || fluidInTank.getAmount() < FluidType.BUCKET_VOLUME) {
                return stack;
            }

            if (!teapot.getTeaFluidId().equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                return stack;
            }

            if (simulate) {
                return ItemStack.EMPTY;
            }

            FluidStack drained = fluidCap.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
            if (drained.getAmount() >= FluidType.BUCKET_VOLUME) {
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(drained.getFluid());
                accessor.kaleidoscopeCompat$setTeaFluidId(fluidId);
                accessor.kaleidoscopeCompat$setCurrentTick(TeapotBlockEntity.INGREDIENT_TIME);
                teapot.setChanged();

                if (teapot.getLevel() != null && !teapot.getLevel().isClientSide) {
                    ((net.minecraft.server.level.ServerLevel) teapot.getLevel()).getChunkSource().blockChanged(teapot.getBlockPos());
                }

                if (teapot.getLevel() != null) {
                    Block.popResource(teapot.getLevel(), teapot.getBlockPos(), new ItemStack(Items.BUCKET));
                }

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

            int status = teapot.getStatus();
            if (status != ITeapot.PUT_INGREDIENT) {
                return false;
            }

            var fluidCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidCap == null) {
                return false;
            }

            FluidStack fluidInTank = fluidCap.getFluidInTank(0);
            if (fluidInTank.isEmpty() || fluidInTank.getAmount() < FluidType.BUCKET_VOLUME) {
                return false;
            }
            return teapot.getTeaFluidId().equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID);
        }
    }
}