package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.TeapotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Optional;

public class CreateTeapotArmCompat {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "teapot");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateTeapotArmCompat::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, TeapotType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class TeapotType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof TeapotBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new TeapotPoint(this, level, pos, state);
        }
    }

    private static class TeapotPoint extends TopFaceArmInteractionPoint {
        public TeapotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            TeapotBlockEntity teapot = getTeapot();
            if (teapot == null || stack.isEmpty()) {
                return stack;
            }

            if (teapot.getStatus() == ITeapot.PUT_INGREDIENT) {
                if (teapot.getTeaFluidId().equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                    return tryInsertTeaFluid(teapot, stack, simulate);
                }

                if (teapot.getInput().isEmpty()) {
                    ItemStack ingredientRemainder = tryInsertIngredient(teapot, stack, simulate);
                    if (ingredientRemainder != stack) {
                        return ingredientRemainder;
                    }
                }

                return tryTakeOutTeaFluid(teapot, stack, simulate);
            }

            if (teapot.getStatus() == ITeapot.FINISHED) {
                return tryPourIntoCup(teapot, stack, simulate);
            }

            return stack;
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 0;
        }

        private ItemStack tryInsertTeaFluid(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
            if (stack.getCount() != 1) {
                return stack;
            }

            return FluidUtil.getFluidHandler(stack.copy()).map(handler -> {
                FluidStack drained = handler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                if (drained.getAmount() < FluidType.BUCKET_VOLUME) {
                    return stack;
                }

                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(drained.getFluid());
                if (fluidId == null) {
                    return stack;
                }

                if (!simulate) {
                    ((TeapotBlockEntityAccessor) teapot).kaleidoscopeCompat$setTeaFluidId(fluidId);
                    teapot.refresh();
                }
                return handler.getContainer();
            }).orElse(stack);
        }

        private ItemStack tryTakeOutTeaFluid(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
            if (stack.getCount() != 1 || !teapot.getInput().isEmpty()) {
                return stack;
            }

            ResourceLocation teaFluidId = teapot.getTeaFluidId();
            if (teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                return stack;
            }

            return FluidUtil.getFluidHandler(stack.copy()).map(handler -> {
                FluidStack toFill = new FluidStack(BuiltInRegistries.FLUID.get(teaFluidId), FluidType.BUCKET_VOLUME);
                int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                if (filled < FluidType.BUCKET_VOLUME) {
                    return stack;
                }

                if (!simulate) {
                    TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
                    accessor.kaleidoscopeCompat$setTeaFluidId(TeapotRecipeSerializer.EMPTY_TEA_FLUID);
                    accessor.kaleidoscopeCompat$setCurrentTick(-1);
                    teapot.refresh();
                }
                return handler.getContainer();
            }).orElse(stack);
        }

        private ItemStack tryInsertIngredient(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
            Optional<RecipeHolder<TeapotRecipe>> recipeOpt = level.getRecipeManager().getRecipeFor(
                    ModRecipes.TEAPOT_RECIPE,
                    new TeapotInput(stack, teapot.getTeaFluidId()),
                    level
            );
            if (recipeOpt.isEmpty()) {
                return stack;
            }

            TeapotRecipe recipe = recipeOpt.get().value();
            int count = recipe.ingredientCount();
            if (stack.getCount() < count) {
                return stack;
            }

            if (!simulate) {
                TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
                accessor.kaleidoscopeCompat$setInput(stack.copyWithCount(count));
                accessor.kaleidoscopeCompat$setCurrentTick(TeapotBlockEntity.INGREDIENT_TIME);
                teapot.refresh();
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(count);
            return remainder;
        }

        private ItemStack tryPourIntoCup(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
            if (!stack.is(ModItems.EMPTY_CUP.get()) || stack.getCount() < 1 || teapot.getResult().isEmpty()) {
                return stack;
            }

            int availableTeaCount = teapot.getResult().getCount();
            if (stack.getCount() > availableTeaCount) {
                if (simulate) {
                    ItemStack remainder = stack.copy();
                    remainder.shrink(availableTeaCount);
                    return remainder;
                }
                return stack;
            }

            ItemStack filledCup = teapot.getResult().copyWithCount(stack.getCount());
            if (!simulate) {
                ItemStack remainingTea = teapot.getResult().copy();
                remainingTea.shrink(stack.getCount());
                if (remainingTea.isEmpty()) {
                    resetAfterFinish(teapot);
                } else {
                    ((TeapotBlockEntityAccessor) teapot).kaleidoscopeCompat$setResult(remainingTea);
                    teapot.refresh();
                }
            }
            return filledCup;
        }

        private void resetAfterFinish(TeapotBlockEntity teapot) {
            TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
            accessor.kaleidoscopeCompat$setInput(ItemStack.EMPTY);
            accessor.kaleidoscopeCompat$setTeaFluidId(TeapotRecipeSerializer.EMPTY_TEA_FLUID);
            accessor.kaleidoscopeCompat$setResult(ItemStack.EMPTY);
            accessor.kaleidoscopeCompat$setStatus(ITeapot.PUT_INGREDIENT);
            accessor.kaleidoscopeCompat$setCurrentTick(-1);
            teapot.refresh();
        }

        private TeapotBlockEntity getTeapot() {
            return level.getBlockEntity(pos) instanceof TeapotBlockEntity teapot ? teapot : null;
        }
    }
}
