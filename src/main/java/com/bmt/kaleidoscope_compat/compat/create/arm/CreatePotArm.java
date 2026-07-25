package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.automation.RecipeAutomation;
import com.bmt.kaleidoscope_compat.compat.create.automation.WorkBlockItemAutomation;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.PotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IPot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CreatePotArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "pot");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreatePotArm::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, PotType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class PotType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof PotBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new PotPoint(this, level, pos, state);
        }
    }

    private static class PotPoint extends TopFaceArmInteractionPoint {
        public PotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            PotBlockEntity pot = getPot();
            if (pot == null) {
                return stack;
            }
            if (RecipeAutomation.isApplicableRecipe(pot, stack)) {
                return RecipeAutomation.tryInsertReusableRecipe(pot, stack, simulate);
            }
            return WorkBlockItemAutomation.insert(level, pos, stack, simulate);
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            PotBlockEntity pot = getPot();
            if (pot == null || slot != 0 || pot.getStatus() != IPot.FINISHED) {
                return ItemStack.EMPTY;
            }

            Ingredient carrier = ((PotBlockEntityAccessor) pot).kaleidoscopeCompat$getCarrier();
            if (!carrier.isEmpty() || pot.getResult().isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack result = pot.getResult().copy();
            if (!simulate) {
                pot.reset();
            }
            return result;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            PotBlockEntity pot = getPot();
            if (pot == null || pot.getStatus() != IPot.FINISHED) {
                return 0;
            }

            Ingredient carrier = ((PotBlockEntityAccessor) pot).kaleidoscopeCompat$getCarrier();
            return carrier.isEmpty() && !pot.getResult().isEmpty() ? 1 : 0;
        }

        private PotBlockEntity getPot() {
            return level.getBlockEntity(pos) instanceof PotBlockEntity pot ? pot : null;
        }
    }
}
