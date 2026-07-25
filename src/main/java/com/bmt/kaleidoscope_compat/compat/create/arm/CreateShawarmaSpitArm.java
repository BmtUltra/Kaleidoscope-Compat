package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.automation.WorkBlockItemAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
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

public class CreateShawarmaSpitArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "shawarma_spit");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateShawarmaSpitArm::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, ShawarmaSpitType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class ShawarmaSpitType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof ShawarmaSpitBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ShawarmaSpitPoint(this, level, pos, state);
        }
    }

    private static class ShawarmaSpitPoint extends TopFaceArmInteractionPoint {
        public ShawarmaSpitPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            return WorkBlockItemAutomation.insert(level, pos, stack, simulate);
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            ShawarmaSpitBlockEntity spit = getSpit();
            if (spit == null || slot != 0 || spit.cookTime > 0 || spit.cookedItem.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack result = spit.cookedItem.copy();
            if (!simulate) {
                spit.cookingItem = ItemStack.EMPTY;
                spit.cookedItem = ItemStack.EMPTY;
                spit.cookTime = 0;
                spit.refresh();
            }
            return result;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            ShawarmaSpitBlockEntity spit = getSpit();
            return spit != null && spit.cookTime <= 0 && !spit.cookedItem.isEmpty() ? 1 : 0;
        }

        private ShawarmaSpitBlockEntity getSpit() {
            return level.getBlockEntity(pos) instanceof ShawarmaSpitBlockEntity spit ? spit : null;
        }
    }
}
