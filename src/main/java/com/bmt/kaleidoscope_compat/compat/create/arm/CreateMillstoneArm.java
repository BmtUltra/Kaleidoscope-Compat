package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.automation.WorkBlockItemAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CreateMillstoneArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "millstone");
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
            return WorkBlockItemAutomation.insert(level, pos, stack, simulate);
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
