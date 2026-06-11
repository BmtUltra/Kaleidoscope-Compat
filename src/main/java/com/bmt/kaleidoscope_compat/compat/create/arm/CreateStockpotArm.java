package com.bmt.kaleidoscope_compat.compat.create.arm;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.StockpotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IStockpot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Unique;

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
            if (stockpot == null || stockpot.hasLid()) {
                return stack;
            }

            if (stockpot.getStatus() == IStockpot.PUT_SOUP_BASE) {
                return tryInsertSoupBase(stockpot, stack, simulate);
            }

            if (stockpot.getStatus() == IStockpot.PUT_INGREDIENT) {
                RecipeItem.RecipeRecord record = getStoredRecipe(stockpot);
                if (record == null || !record.type().equals(RecipeItem.STOCKPOT)) {
                    return stack;
                }

                if (stack.is(ModItems.STOCKPOT_LID.get())) {
                    return tryInsertLid(stockpot, record, stack, simulate);
                }

                return tryInsertIngredient(stockpot, record, stack, simulate);
            }

            if (stockpot.getStatus() == IStockpot.FINISHED) {
                return tryTakeOutProduct(stockpot, stack, simulate);
            }

            return stack;
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
                    && getCarrier(stockpot).isEmpty() && !stockpot.getResult().isEmpty()
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
                    && getCarrier(stockpot).isEmpty() && !stockpot.getResult().isEmpty()
                    && stockpot.getTakeoutCount() > 0) {
                return 1;
            }

            return 0;
        }

        @Unique
        private Ingredient getCarrier(StockpotBlockEntity stockpot) {
            StockpotBlockEntityAccessor accessor = (StockpotBlockEntityAccessor) stockpot;
            ResourceLocation recipeId = accessor.kaleidoscopeCompat$getRecipeId();
            if (recipeId == null || recipeId.equals(StockpotRecipeSerializer.EMPTY_ID)) {
                return Ingredient.EMPTY;
            }

            RecipeManager recipeManager = level.getRecipeManager();
            RecipeHolder<StockpotRecipe> recipeHolder = recipeManager.getRecipeFor(
                    ModRecipes.STOCKPOT_RECIPE,
                    new StockpotInput(stockpot.getInputs(), accessor.kaleidoscopeCompat$getSoupBaseId()),
                    level
            ).orElse(null);

            if (recipeHolder != null) {
                return recipeHolder.value().carrier();
            }
            return Ingredient.EMPTY;
        }

        private ItemStack tryInsertSoupBase(StockpotBlockEntity stockpot, ItemStack stack, boolean simulate) {
            Item containerItem = ItemUtils.getContainerItem(stack);
            if (containerItem == Items.AIR || stack.getCount() != 1) {
                return stack;
            }

            for (var entry : SoupBaseManager.getAllSoupBases().entrySet()) {
                if (!entry.getValue().isSoupBase(stack)) {
                    continue;
                }

                if (!simulate) {
                    ((StockpotBlockEntityAccessor) stockpot).kaleidoscopeCompat$setSoupBaseId(entry.getKey());
                    stockpot.setStatus(IStockpot.PUT_INGREDIENT);
                    stockpot.refresh();
                }
                return containerItem.getDefaultInstance();
            }

            return stack;
        }

        private ItemStack tryInsertIngredient(StockpotBlockEntity stockpot, RecipeItem.RecipeRecord record,
                                              ItemStack stack, boolean simulate) {
            if (stack.is(TagMod.INGREDIENT_BLOCKLIST)) {
                return stack;
            }

            Reference2IntMap<Item> remaining = getRemainingIngredients(record, stockpot);
            if (remaining.isEmpty()) {
                return stack;
            }

            Item stackItem = stack.getItem();
            if (remaining.getInt(stackItem) <= 0) {
                return stack;
            }

            int emptySlot = getFirstEmptySlot(stockpot);
            if (emptySlot == -1) {
                return stack;
            }

            Item containerItem = ItemUtils.getContainerItem(stack);
            if (containerItem != Items.AIR && stack.getCount() != 1) {
                return stack;
            }

            if (!simulate) {
                stockpot.getInputs().set(emptySlot, stack.copyWithCount(1));
                stockpot.refresh();
            }

            if (containerItem != Items.AIR) {
                return containerItem.getDefaultInstance();
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        private ItemStack tryInsertLid(StockpotBlockEntity stockpot, RecipeItem.RecipeRecord record,
                                       ItemStack stack, boolean simulate) {
            if (stack.getCount() != 1 || !recipeFullyMatched(record, stockpot)) {
                return stack;
            }

            if (!simulate) {
                stockpot.setLidItem(stack.copyWithCount(1));
                setHasLid(stockpot, true);
                stockpot.refresh();
            }

            return ItemStack.EMPTY;
        }

        private ItemStack tryTakeOutProduct(StockpotBlockEntity stockpot, ItemStack stack, boolean simulate) {
            if (stockpot.getResult().isEmpty() || stockpot.getTakeoutCount() <= 0) {
                return stack;
            }

            Ingredient carrier = getCarrier(stockpot);
            if (carrier.isEmpty() || !carrier.test(stack)) {
                return stack;
            }

            int takeoutCount = stockpot.getTakeoutCount();
            if (stack.getCount() < 1) {
                return stack;
            }

            if (simulate && stack.getCount() > 1 && stack.getCount() != takeoutCount) {
                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                return remainder;
            }

            if (stack.getCount() != 1 && stack.getCount() != takeoutCount) {
                return stack;
            }

            int produced = stack.getCount() == takeoutCount ? takeoutCount : 1;
            ItemStack result = stockpot.getResult().copyWithCount(produced);
            if (!simulate) {
                consumeFinishedResult(stockpot, produced);
            }
            return result;
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

        private boolean recipeFullyMatched(RecipeItem.RecipeRecord record, StockpotBlockEntity stockpot) {
            return getRemainingIngredients(record, stockpot).isEmpty() && !hasUnexpectedIngredients(record, stockpot);
        }

        private Reference2IntMap<Item> getRemainingIngredients(RecipeItem.RecipeRecord record, StockpotBlockEntity stockpot) {
            Reference2IntMap<Item> remaining = new Reference2IntOpenHashMap<>();
            for (ItemStack input : record.input()) {
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    remaining.put(item, remaining.getInt(item) + 1);
                }
            }

            for (ItemStack existing : stockpot.getInputs()) {
                if (existing.isEmpty()) {
                    continue;
                }
                Item item = existing.getItem();
                remaining.put(item, remaining.getInt(item) - 1);
            }

            remaining.reference2IntEntrySet().removeIf(entry -> entry.getIntValue() <= 0);
            return remaining;
        }

        private boolean hasUnexpectedIngredients(RecipeItem.RecipeRecord record, StockpotBlockEntity stockpot) {
            Reference2IntMap<Item> expected = new Reference2IntOpenHashMap<>();
            for (ItemStack input : record.input()) {
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    expected.put(item, expected.getInt(item) + 1);
                }
            }

            for (ItemStack existing : stockpot.getInputs()) {
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

        private int getFirstEmptySlot(StockpotBlockEntity stockpot) {
            for (int i = 0; i < stockpot.getInputs().size(); i++) {
                if (stockpot.getInputs().get(i).isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        private RecipeItem.RecipeRecord getStoredRecipe(StockpotBlockEntity stockpot) {
            return stockpot instanceof StockpotArmAutomation automation ? automation.kaleidoscopeCompat$getStoredRecipe() : null;
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