package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.automation.WorkBlockItemAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CreateSteamerArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "steamer");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateSteamerArm::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, SteamerType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class SteamerType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof SteamerBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new SteamerPoint(this, level, pos, state);
        }
    }

    private static class SteamerPoint extends TopFaceArmInteractionPoint {
        public SteamerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            return WorkBlockItemAutomation.insert(level, pos, stack, simulate);
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            SteamerBlockEntity steamer = getSteamer();
            if (steamer == null || isBlockedAbove()) {
                return ItemStack.EMPTY;
            }

            int readySlot = getReadySlotByIndex(steamer, slot);
            if (readySlot == -1) {
                return ItemStack.EMPTY;
            }

            ItemStack template = steamer.getItems().get(readySlot).copy();
            int extractLimit = Math.min(amount, template.getMaxStackSize());
            if (extractLimit <= 0) {
                return ItemStack.EMPTY;
            }
            int extracted = countExtractableReadyItems(steamer, template, readySlot, extractLimit);
            ItemStack result = template.copyWithCount(extracted);
            if (!simulate) {
                consumeReadyItems(steamer, template, readySlot, extracted);
                steamer.refresh();
            }
            return result;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            SteamerBlockEntity steamer = getSteamer();
            if (steamer == null || isBlockedAbove()) {
                return 0;
            }

            int readyCount = 0;
            NonNullList<ItemStack> items = steamer.getItems();
            int[] cookingTime = steamer.getCookingTime();
            int endIndex = getEndIndex(steamer);
            for (int i = 0; i < endIndex; i++) {
                if (!items.get(i).isEmpty() && cookingTime[i] == -1) {
                    readyCount++;
                }
            }
            return readyCount;
        }

        private int getReadySlotByIndex(SteamerBlockEntity steamer, int slot) {
            if (slot < 0) {
                return -1;
            }

            NonNullList<ItemStack> items = steamer.getItems();
            int[] cookingTime = steamer.getCookingTime();
            int endIndex = getEndIndex(steamer);
            int readyIndex = 0;
            for (int i = 0; i < endIndex; i++) {
                if (items.get(i).isEmpty() || cookingTime[i] != -1) {
                    continue;
                }
                if (readyIndex == slot) {
                    return i;
                }
                readyIndex++;
            }
            return -1;
        }

        private int countExtractableReadyItems(SteamerBlockEntity steamer, ItemStack template, int startSlot, int limit) {
            NonNullList<ItemStack> items = steamer.getItems();
            int[] cookingTime = steamer.getCookingTime();
            int endIndex = getEndIndex(steamer);
            int count = 0;
            for (int i = startSlot; i < endIndex && count < limit; i++) {
                if (cookingTime[i] != -1 || !ItemStack.isSameItemSameComponents(items.get(i), template)) {
                    continue;
                }
                count += items.get(i).getCount();
            }
            return Math.min(count, limit);
        }

        private void consumeReadyItems(SteamerBlockEntity steamer, ItemStack template, int startSlot, int amount) {
            NonNullList<ItemStack> items = steamer.getItems();
            int[] cookingProgress = steamer.getCookingProgress();
            int[] cookingTime = steamer.getCookingTime();
            int endIndex = getEndIndex(steamer);
            int left = amount;
            for (int i = startSlot; i < endIndex && left > 0; i++) {
                if (cookingTime[i] != -1 || !ItemStack.isSameItemSameComponents(items.get(i), template)) {
                    continue;
                }
                int taken = Math.min(left, items.get(i).getCount());
                left -= taken;
                items.get(i).shrink(taken);
                if (items.get(i).isEmpty()) {
                    items.set(i, ItemStack.EMPTY);
                    cookingProgress[i] = 0;
                    cookingTime[i] = 0;
                }
            }
        }

        private int getEndIndex(SteamerBlockEntity steamer) {
            return steamer.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
        }

        private boolean isBlockedAbove() {
            BlockPos above = pos.above();
            return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
        }

        private SteamerBlockEntity getSteamer() {
            return level.getBlockEntity(pos) instanceof SteamerBlockEntity steamer ? steamer : null;
        }
    }
}
