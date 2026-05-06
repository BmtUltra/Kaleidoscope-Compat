package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CreateShawarmaSpitArmCompat {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "shawarma_spit");
    private static final int MAX_ITEMS = 8;
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateShawarmaSpitArmCompat::register);
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
            return state.hasProperty(ShawarmaSpitBlock.HALF)
                    && state.getValue(ShawarmaSpitBlock.HALF) == DoubleBlockHalf.UPPER
                    && level.getBlockEntity(pos.below()) instanceof ShawarmaSpitBlockEntity;
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
            ShawarmaSpitBlockEntity spit = getSpit();
            if (spit == null || stack.isEmpty() || !spit.cookingItem.isEmpty() || !spit.cookedItem.isEmpty()) {
                return stack;
            }

            SingleRecipeInput input = new SingleRecipeInput(stack);
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level);
            if (recipe.isEmpty()) {
                return stack;
            }

            int insertedCount = Math.min(stack.getCount(), MAX_ITEMS);
            ItemStack remainder = stack.copy();
            remainder.shrink(insertedCount);

            if (!simulate) {
                spit.cookingItem = stack.copyWithCount(insertedCount);
                spit.cookedItem = recipe.get().value().assemble(input, level.registryAccess());
                spit.cookedItem.setCount(insertedCount);
                spit.cookTime = recipe.get().value().getCookingTime();
                spit.refresh();
            }

            return remainder;
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
            BlockPos lowerPos = pos.below();
            return level.getBlockEntity(lowerPos) instanceof ShawarmaSpitBlockEntity spit ? spit : null;
        }
    }
}
