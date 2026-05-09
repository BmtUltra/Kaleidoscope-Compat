package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Optional;

public class CreateSteamerArmCompat {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "steamer");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateSteamerArmCompat::register);
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
            SteamerBlockEntity steamer = getSteamer();
            if (steamer == null || stack.isEmpty() || isBlockedAbove()) {
                return stack;
            }

            Optional<? extends RecipeHolder<?>> recipe = steamer.getSteamerRecipe(level, stack);
            if (recipe.isEmpty()) {
                return stack;
            }

            int emptySlotCount = getEmptySlotCount(steamer);
            if (emptySlotCount == 0) {
                return stack;
            }

            int cookTime = steamer.getSteamerRecipe(level, stack)
                    .map(holder -> holder.value().getCookTick())
                    .orElse(0);
            if (cookTime <= 0) {
                return stack;
            }

            int insertCount = Math.min(stack.getCount(), emptySlotCount);
            if (!simulate) {
                NonNullList<ItemStack> items = steamer.getItems();
                int[] cookingProgress = steamer.getCookingProgress();
                int[] cookingTime = steamer.getCookingTime();
                int endIndex = getEndIndex(steamer);
                int left = insertCount;
                for (int i = 0; i < endIndex && left > 0; i++) {
                    if (!items.get(i).isEmpty()) {
                        continue;
                    }
                    items.set(i, stack.copyWithCount(1));
                    cookingProgress[i] = 0;
                    cookingTime[i] = cookTime;
                    left--;
                }
                steamer.refresh();
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(insertCount);
            return remainder;
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

            ItemStack template = steamer.getItems().get(readySlot);
            int extractLimit = amount > 0 ? amount : template.getMaxStackSize();
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

        private int getEmptySlotCount(SteamerBlockEntity steamer) {
            NonNullList<ItemStack> items = steamer.getItems();
            int endIndex = getEndIndex(steamer);
            int emptyCount = 0;
            for (int i = 0; i < endIndex; i++) {
                if (items.get(i).isEmpty()) {
                    emptyCount++;
                }
            }
            return emptyCount;
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
