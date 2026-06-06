package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.PotBlockEntityAccessor;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IPot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.TopFaceArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CreatePotArmCompat {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "pot");
    private static final int PUT_INGREDIENT_TIME = 60 * 20;
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreatePotArmCompat::register);
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

            RecipeItem.RecipeRecord record = getStoredRecipe(pot);
            if (record == null || !record.type().equals(RecipeItem.POT)) {
                return stack;
            }

            if (!hasOil(pot)) {
                return tryInsertOil(pot, stack, simulate);
            }

            if (pot.getStatus() == IPot.PUT_INGREDIENT) {
                if (stack.is(TagMod.KITCHEN_SHOVEL)) {
                    return tryUseShovel(pot, record, stack, simulate);
                }
                return tryInsertIngredient(pot, record, stack, simulate);
            }

            if (pot.getStatus() == IPot.COOKING && stack.is(TagMod.KITCHEN_SHOVEL)) {
                return tryUseShovel(pot, record, stack, simulate);
            }

            if (pot.getStatus() == IPot.FINISHED) {
                return tryTakeOutProduct(pot, stack, simulate);
            }

            return stack;
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

        private ItemStack tryInsertOil(PotBlockEntity pot, ItemStack stack, boolean simulate) {
            if (stack.is(ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
                if (simulate) {
                    ItemStack simulated = stack.copy();
                    KitchenShovelItem.setHasOil(simulated, false);
                    return simulated;
                }

                ItemStack remainder = stack.copy();
                KitchenShovelItem.setHasOil(remainder, false);
                placeOil(pot);
                return remainder;
            }

            if (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
                ItemStack remainder = stack.copy();
                OilPotItem.shrinkOilCount(remainder);
                if (!simulate) {
                    placeOil(pot);
                }
                return remainder;
            }

            if (stack.is(TagMod.OIL) || stack.is(TagUtil.Items.BOTTLE_OIL) || stack.is(TagUtil.Items.BUCKET_OIL)) {
                Item containerItem = ItemUtils.getContainerItem(stack);
                if (containerItem != Items.AIR) {
                    if (stack.getCount() != 1) {
                        return stack;
                    }
                    if (!simulate) {
                        placeOil(pot);
                    }
                    return containerItem.getDefaultInstance();
                }

                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                if (!simulate) {
                    placeOil(pot);
                }
                return remainder;
            }

            return stack;
        }

        private ItemStack tryInsertIngredient(PotBlockEntity pot, RecipeItem.RecipeRecord record,
                                              ItemStack stack, boolean simulate) {
            if (stack.is(TagMod.INGREDIENT_BLOCKLIST)) {
                return stack;
            }

            Reference2IntMap<Item> remaining = getRemainingIngredients(record, pot);
            if (remaining.isEmpty()) {
                return stack;
            }

            Item stackItem = stack.getItem();
            if (remaining.getInt(stackItem) <= 0) {
                return stack;
            }

            int emptySlot = getFirstEmptySlot(pot);
            if (emptySlot == -1) {
                return stack;
            }

            Item containerItem = ItemUtils.getContainerItem(stack);
            if (containerItem != Items.AIR && stack.getCount() != 1) {
                return stack;
            }

            if (!simulate) {
                pot.getInputs().set(emptySlot, stack.copyWithCount(1));
                pot.refresh();
            }

            if (containerItem != Items.AIR) {
                return containerItem.getDefaultInstance();
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        private ItemStack tryUseShovel(PotBlockEntity pot, RecipeItem.RecipeRecord record,
                                       ItemStack stack, boolean simulate) {
            if (!stack.is(TagMod.KITCHEN_SHOVEL)) {
                return stack;
            }

            if (pot.getStatus() == IPot.PUT_INGREDIENT) {
                if (!recipeFullyMatched(record, pot)) {
                    return stack;
                }
                if (simulate) {
                    return stack.copyWithCount(Math.max(0, stack.getCount() - 1));
                }
                PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
                accessor.kaleidoscopeCompat$setSeed(System.currentTimeMillis());
                accessor.kaleidoscopeCompat$invokeStartCooking(level);
                return stack;
            }

            if (pot.getStatus() == IPot.COOKING) {
                PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
                int stirFryCount = accessor.kaleidoscopeCompat$getStirFryCount();
                if (stirFryCount <= 0) {
                    return stack;
                }
                if (simulate) {
                    return stack.copyWithCount(Math.max(0, stack.getCount() - 1));
                }
                accessor.kaleidoscopeCompat$setSeed(System.currentTimeMillis());
                accessor.kaleidoscopeCompat$setStirFryCount(stirFryCount - 1);
                pot.refresh();
                return stack;
            }

            return stack;
        }

        private ItemStack tryTakeOutProduct(PotBlockEntity pot, ItemStack stack, boolean simulate) {
            if (pot.getResult().isEmpty()) {
                return stack;
            }

            Ingredient carrier = ((PotBlockEntityAccessor) pot).kaleidoscopeCompat$getCarrier();
            if (carrier.isEmpty() || !carrier.test(stack)) {
                return stack;
            }

            int requiredCount = pot.getResult().getCount();
            if (simulate && stack.getCount() > requiredCount) {
                ItemStack remainder = stack.copy();
                remainder.shrink(requiredCount);
                return remainder;
            }

            if (stack.getCount() != requiredCount) {
                return stack;
            }

            ItemStack result = pot.getResult().copy();
            if (!simulate) {
                pot.reset();
            }
            return result;
        }

        private boolean recipeFullyMatched(RecipeItem.RecipeRecord record, PotBlockEntity pot) {
            return getRemainingIngredients(record, pot).isEmpty() && !hasUnexpectedIngredients(record, pot);
        }

        private Reference2IntMap<Item> getRemainingIngredients(RecipeItem.RecipeRecord record, PotBlockEntity pot) {
            Reference2IntMap<Item> remaining = new Reference2IntOpenHashMap<>();
            for (ItemStack input : record.input()) {
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    remaining.put(item, remaining.getInt(item) + 1);
                }
            }

            for (ItemStack existing : pot.getInputs()) {
                if (existing.isEmpty()) {
                    continue;
                }
                Item item = existing.getItem();
                remaining.put(item, remaining.getInt(item) - 1);
            }

            remaining.reference2IntEntrySet().removeIf(entry -> entry.getIntValue() <= 0);
            return remaining;
        }

        private boolean hasUnexpectedIngredients(RecipeItem.RecipeRecord record, PotBlockEntity pot) {
            Reference2IntMap<Item> expected = new Reference2IntOpenHashMap<>();
            for (ItemStack input : record.input()) {
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    expected.put(item, expected.getInt(item) + 1);
                }
            }

            for (ItemStack existing : pot.getInputs()) {
                if (existing.isEmpty()) {
                    continue;
                }
                Item item = existing.getItem();
                int left = expected.getInt(item) - 1;
                if (left < 0) {
                    return true;
                }
                expected.put(item, left);
            }

            return false;
        }

        private int getFirstEmptySlot(PotBlockEntity pot) {
            for (int i = 0; i < pot.getInputs().size(); i++) {
                if (pot.getInputs().get(i).isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        private RecipeItem.RecipeRecord getStoredRecipe(PotBlockEntity pot) {
            return pot instanceof PotArmAutomation automation ? automation.kaleidoscopeCompat$getStoredRecipe() : null;
        }

        private boolean hasOil(PotBlockEntity pot) {
            BlockState state = pot.getBlockState();
            return state.hasProperty(PotBlock.HAS_OIL) && state.getValue(PotBlock.HAS_OIL);
        }

        private void placeOil(PotBlockEntity pot) {
            PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
            accessor.kaleidoscopeCompat$setCurrentTick(PUT_INGREDIENT_TIME);
            accessor.kaleidoscopeCompat$setStatus(IPot.PUT_INGREDIENT);
            BlockState state = pot.getBlockState();
            if (state.hasProperty(PotBlock.HAS_OIL) && state.hasProperty(PotBlock.SHOW_OIL)) {
                level.setBlockAndUpdate(pos, state.setValue(PotBlock.HAS_OIL, true).setValue(PotBlock.SHOW_OIL, true));
            }
            pot.refresh();
        }

        private PotBlockEntity getPot() {
            return level.getBlockEntity(pos) instanceof PotBlockEntity pot ? pot : null;
        }
    }
}
