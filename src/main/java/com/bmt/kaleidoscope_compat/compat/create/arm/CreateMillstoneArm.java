package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Optional;

public class CreateMillstoneArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "millstone");
    private static final int MAX_INPUT_COUNT = 8;
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateMillstoneArm::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, MillstoneType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class MillstoneType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof MillstoneBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new MillstonePoint(this, level, pos, state);
        }
    }

    private static class MillstonePoint extends TopFaceArmInteractionPoint {
        public MillstonePoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            MillstoneBlockEntity millstone = getMillstone();
            if (millstone == null || stack.isEmpty() || !millstone.getInput().isEmpty() || !millstone.isOutputEmpty()) {
                return stack;
            }

            int insertCount = Math.min(stack.getCount(), MAX_INPUT_COUNT);
            ItemStack toInsert = stack.copyWithCount(insertCount);
            SimpleInput simpleInput = new SimpleInput(List.of(toInsert));
            Optional<RecipeHolder<MillstoneRecipe>> recipe = millstone.matchRecipe(simpleInput, level);
            if (recipe.isEmpty()) {
                return stack;
            }

            if (!simulate && !millstone.onPutItem(level, stack)) {
                return stack;
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(insertCount);
            return remainder;
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            MillstoneBlockEntity millstone = getMillstone();
            if (millstone == null || slot != 0 || millstone.isOutputEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack result = millstone.getOutputs().getStackInSlot(0).copy();
            if (!simulate) {
                millstone.resetWhenTakeout();
            }
            return result;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            MillstoneBlockEntity millstone = getMillstone();
            return millstone != null && !millstone.isOutputEmpty() ? 1 : 0;
        }

        private MillstoneBlockEntity getMillstone() {
            return level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone ? millstone : null;
        }
    }
}