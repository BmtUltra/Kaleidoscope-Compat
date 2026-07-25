package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.automation.RecipeAutomation;
import com.bmt.kaleidoscope_compat.compat.create.automation.WorkBlockItemAutomation;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.StockpotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IStockpot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
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

public class CreateStockpotArm {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "stockpot");
    private static boolean initialized;

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        modEventBus.addListener(CreateStockpotArm::register);
        initialized = true;
    }

    private static void register(RegisterEvent event) {
        if (!CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.containsKey(ID)) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), ID, StockpotType::new);
            ArmInteractionPointType.init();
        }
    }

    private static class StockpotType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof StockpotBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new StockpotPoint(this, level, pos, state);
        }
    }

    private static class StockpotPoint extends TopFaceArmInteractionPoint {
        public StockpotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            StockpotBlockEntity stockpot = getStockpot();
            if (stockpot == null) {
                return stack;
            }
            if (RecipeAutomation.isApplicableRecipe(stockpot, stack)) {
                return RecipeAutomation.tryInsertReusableRecipe(stockpot, stack, simulate);
            }
            return WorkBlockItemAutomation.insert(level, pos, stack, simulate);
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            StockpotBlockEntity stockpot = getStockpot();
            if (stockpot == null || slot != 0) {
                return ItemStack.EMPTY;
            }

            if (stockpot.hasLid() && stockpot.getStatus() == IStockpot.FINISHED) {
                ItemStack lid = stockpot.getLidItem().isEmpty()
                        ? ModItems.STOCKPOT_LID.get().getDefaultInstance()
                        : stockpot.getLidItem().copy();
                if (!simulate) {
                    stockpot.setLidItem(ItemStack.EMPTY);
                    setHasLid(stockpot, false);
                    stockpot.refresh();
                }
                return lid;
            }

            if (!stockpot.hasLid() && stockpot.getStatus() == IStockpot.FINISHED
                    && WorkBlockItemAutomation.getStockpotCarrier(level, stockpot).isEmpty()
                    && stockpot.getTakeoutCount() > 0) {
                int extractCount = amount > 0 ? Math.min(amount, stockpot.getTakeoutCount()) : stockpot.getTakeoutCount();
                ItemStack result = stockpot.getResult().copyWithCount(extractCount);
                if (!simulate) {
                    consumeFinishedResult(stockpot, extractCount);
                }
                return result;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            StockpotBlockEntity stockpot = getStockpot();
            if (stockpot == null) {
                return 0;
            }

            if (stockpot.hasLid() && stockpot.getStatus() == IStockpot.FINISHED) {
                return 1;
            }

            if (!stockpot.hasLid() && stockpot.getStatus() == IStockpot.FINISHED
                    && WorkBlockItemAutomation.getStockpotCarrier(level, stockpot).isEmpty()
                    && stockpot.getTakeoutCount() > 0) {
                return 1;
            }

            return 0;
        }

        private void consumeFinishedResult(StockpotBlockEntity stockpot, int amount) {
            StockpotBlockEntityAccessor accessor = (StockpotBlockEntityAccessor) stockpot;
            int remaining = stockpot.getTakeoutCount() - amount;
            accessor.kaleidoscopeCompat$setTakeoutCount(remaining);
            if (remaining > 0) {
                stockpot.refresh();
                return;
            }

            stockpot.setStatus(IStockpot.PUT_SOUP_BASE);
            stockpot.getInputs().clear();
            accessor.kaleidoscopeCompat$setRecipeId(StockpotRecipeSerializer.EMPTY_ID);
            accessor.kaleidoscopeCompat$setSoupBaseId(ModSoupBases.WATER);
            accessor.kaleidoscopeCompat$setResult(ItemStack.EMPTY);
            accessor.kaleidoscopeCompat$setCurrentTick(-1);
            stockpot.renderEntity = null;
            stockpot.refresh();
        }

        private void setHasLid(StockpotBlockEntity stockpot, boolean hasLid) {
            BlockState state = stockpot.getBlockState();
            if (state.hasProperty(StockpotBlock.HAS_LID)) {
                level.setBlockAndUpdate(pos, state.setValue(StockpotBlock.HAS_LID, hasLid));
            }
        }

        private StockpotBlockEntity getStockpot() {
            return level.getBlockEntity(pos) instanceof StockpotBlockEntity stockpot ? stockpot : null;
        }
    }
}
