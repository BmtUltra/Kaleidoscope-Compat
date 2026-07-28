package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.PotBlockEntityAccessor;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.StockpotBlockEntityAccessor;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.TeapotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IPot;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IStockpot;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.*;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.FlexStockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Optional;

public final class WorkBlockItemAutomation {
    private static final int POT_PUT_INGREDIENT_TIME = 60 * 20;
    private static final int MILLSTONE_MAX_INPUT_COUNT = 8;
    private static final int SHAWARMA_MAX_ITEMS = 8;

    private WorkBlockItemAutomation() {
    }

    public static ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PotBlockEntity pot) {
            return insertPot(level, pos, pot, stack, simulate);
        }
        if (blockEntity instanceof StockpotBlockEntity stockpot) {
            return insertStockpot(level, pos, stockpot, stack, simulate);
        }
        if (blockEntity instanceof SteamerBlockEntity steamer) {
            return insertSteamer(level, pos, steamer, stack, simulate);
        }
        if (blockEntity instanceof MillstoneBlockEntity millstone) {
            return insertMillstone(level, millstone, stack, simulate);
        }
        if (blockEntity instanceof TeapotBlockEntity teapot) {
            return insertTeapot(level, teapot, stack, simulate);
        }
        if (blockEntity instanceof ShawarmaSpitBlockEntity spit) {
            return insertShawarmaSpit(level, spit, stack, simulate);
        }
        return stack;
    }

    private static ItemStack insertPot(Level level, BlockPos pos, PotBlockEntity pot, ItemStack stack, boolean simulate) {
        RecipeItem.RecipeRecord recipe = RecipeAutomation.getStoredRecipe(pot);
        if (recipe == null || !recipe.type().equals(RecipeItem.POT)) {
            return stack;
        }
        if (!hasOil(pot)) {
            return insertPotOil(level, pos, pot, stack, simulate);
        }
        if (pot.getStatus() == IPot.PUT_INGREDIENT) {
            return stack.is(TagMod.KITCHEN_SHOVEL)
                    ? usePotShovel(level, pot, recipe, stack, simulate)
                    : insertIngredient(pot.getInputs(), pot::refresh, recipe, stack, simulate);
        }
        if (pot.getStatus() == IPot.COOKING && stack.is(TagMod.KITCHEN_SHOVEL)) {
            return usePotShovel(level, pot, recipe, stack, simulate);
        }
        if (pot.getStatus() == IPot.FINISHED) {
            return takePotProduct(pot, stack, simulate);
        }
        return stack;
    }

    private static ItemStack insertPotOil(Level level, BlockPos pos, PotBlockEntity pot, ItemStack stack, boolean simulate) {
        if (stack.is(ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
            ItemStack remainder = stack.copy();
            KitchenShovelItem.setHasOil(remainder, false);
            if (!simulate) {
                placeOil(level, pos, pot);
            }
            return remainder;
        }
        if (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            ItemStack remainder = stack.copy();
            OilPotItem.shrinkOilCount(remainder);
            if (!simulate) {
                placeOil(level, pos, pot);
            }
            return remainder;
        }
        if (!stack.is(TagMod.OIL)) {
            return stack;
        }

        Item containerItem = ItemUtils.getContainerItem(stack);
        if (containerItem != Items.AIR) {
            if (stack.getCount() != 1) {
                return stack;
            }
            if (!simulate) {
                placeOil(level, pos, pot);
            }
            return containerItem.getDefaultInstance();
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        if (!simulate) {
            placeOil(level, pos, pot);
        }
        return remainder;
    }

    private static ItemStack usePotShovel(Level level, PotBlockEntity pot, RecipeItem.RecipeRecord recipe,
                                          ItemStack stack, boolean simulate) {
        if (pot.getStatus() == IPot.PUT_INGREDIENT) {
            if (!recipeFullyMatched(recipe, pot.getInputs())) {
                return stack;
            }
            if (simulate) {
                return acceptedReusableToolSignal(stack);
            }
            PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
            accessor.kaleidoscopeCompat$setSeed(System.currentTimeMillis());
            accessor.kaleidoscopeCompat$invokeStartCooking(level);
            return stack;
        }
        if (pot.getStatus() != IPot.COOKING) {
            return stack;
        }

        PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
        int stirFryCount = accessor.kaleidoscopeCompat$getStirFryCount();
        if (stirFryCount <= 0) {
            return stack;
        }
        if (simulate) {
            return acceptedReusableToolSignal(stack);
        }
        accessor.kaleidoscopeCompat$setSeed(System.currentTimeMillis());
        accessor.kaleidoscopeCompat$setStirFryCount(stirFryCount - 1);
        pot.refresh();
        return stack;
    }

    private static ItemStack takePotProduct(PotBlockEntity pot, ItemStack stack, boolean simulate) {
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

    private static void placeOil(Level level, BlockPos pos, PotBlockEntity pot) {
        PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;
        accessor.kaleidoscopeCompat$setCurrentTick(POT_PUT_INGREDIENT_TIME);
        accessor.kaleidoscopeCompat$setStatus(IPot.PUT_INGREDIENT);
        BlockState state = pot.getBlockState();
        if (state.hasProperty(PotBlock.HAS_OIL) && state.hasProperty(PotBlock.SHOW_OIL)) {
            level.setBlockAndUpdate(pos, state.setValue(PotBlock.HAS_OIL, true).setValue(PotBlock.SHOW_OIL, true));
        }
        pot.refresh();
    }

    private static boolean hasOil(PotBlockEntity pot) {
        BlockState state = pot.getBlockState();
        return state.hasProperty(PotBlock.HAS_OIL) && state.getValue(PotBlock.HAS_OIL);
    }

    private static ItemStack insertStockpot(Level level, BlockPos pos, StockpotBlockEntity stockpot,
                                            ItemStack stack, boolean simulate) {
        if (stockpot.hasLid()) {
            return stack;
        }
        if (stockpot.getStatus() == IStockpot.PUT_SOUP_BASE) {
            return insertSoupBase(stockpot, stack, simulate);
        }
        if (stockpot.getStatus() == IStockpot.PUT_INGREDIENT) {
            RecipeItem.RecipeRecord recipe = RecipeAutomation.getStoredRecipe(stockpot);
            if (recipe == null || !recipe.type().equals(RecipeItem.STOCKPOT)) {
                return stack;
            }
            if (stack.is(ModItems.STOCKPOT_LID.get())) {
                return insertStockpotLid(level, pos, stockpot, recipe, stack, simulate);
            }
            return insertIngredient(stockpot.getInputs(), stockpot::refresh, recipe, stack, simulate);
        }
        if (stockpot.getStatus() == IStockpot.FINISHED) {
            return takeStockpotProduct(level, stockpot, stack, simulate);
        }
        return stack;
    }

    private static ItemStack insertSoupBase(StockpotBlockEntity stockpot, ItemStack stack, boolean simulate) {
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

    private static ItemStack insertStockpotLid(Level level, BlockPos pos, StockpotBlockEntity stockpot,
                                               RecipeItem.RecipeRecord recipe, ItemStack stack, boolean simulate) {
        if (stack.getCount() != 1 || !recipeFullyMatched(recipe, stockpot.getInputs())) {
            return stack;
        }
        if (!simulate) {
            stockpot.setLidItem(stack.copyWithCount(1));
            BlockState state = stockpot.getBlockState();
            if (state.hasProperty(StockpotBlock.HAS_LID)) {
                level.setBlockAndUpdate(pos, state.setValue(StockpotBlock.HAS_LID, true));
            }
            stockpot.refresh();
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack takeStockpotProduct(Level level, StockpotBlockEntity stockpot,
                                                 ItemStack stack, boolean simulate) {
        if (stockpot.getResult().isEmpty() || stockpot.getTakeoutCount() <= 0) {
            return stack;
        }
        Ingredient carrier = getStockpotCarrier(level, stockpot);
        if (carrier.isEmpty() || !carrier.test(stack)) {
            return stack;
        }
        int takeoutCount = stockpot.getTakeoutCount();
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
            consumeStockpotResult(stockpot, produced);
        }
        return result;
    }

    public static Ingredient getStockpotCarrier(Level level, StockpotBlockEntity stockpot) {
        StockpotBlockEntityAccessor accessor = (StockpotBlockEntityAccessor) stockpot;
        ResourceLocation recipeId = accessor.kaleidoscopeCompat$getRecipeId();
        if (recipeId == null || recipeId.equals(StockpotRecipeSerializer.EMPTY_ID)) {
            return StockpotRecipeSerializer.DEFAULT_CARRIER;
        }

        RecipeManager recipeManager = level.getRecipeManager();
        RecipeHolder<StockpotRecipe> recipe = recipeManager.byKeyTyped(ModRecipes.STOCKPOT_RECIPE, recipeId);
        if (recipe != null) {
            return recipe.value().carrier();
        }

        RecipeHolder<FlexStockpotRecipe> flexRecipe = recipeManager.byKeyTyped(ModRecipes.FLEX_STOCKPOT_RECIPE, recipeId);
        return flexRecipe == null ? StockpotRecipeSerializer.DEFAULT_CARRIER : flexRecipe.value().carrier();
    }

    private static void consumeStockpotResult(StockpotBlockEntity stockpot, int amount) {
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

    private static ItemStack insertIngredient(List<ItemStack> inputs, Runnable refresh,
                                              RecipeItem.RecipeRecord recipe, ItemStack stack, boolean simulate) {
        if (stack.is(TagMod.INGREDIENT_BLOCKLIST)) {
            return stack;
        }
        Reference2IntMap<Item> remaining = remainingIngredients(recipe, inputs);
        if (remaining.getInt(stack.getItem()) <= 0) {
            return stack;
        }
        int emptySlot = firstEmptySlot(inputs);
        if (emptySlot == -1) {
            return stack;
        }
        Item containerItem = ItemUtils.getContainerItem(stack);
        if (containerItem != Items.AIR && stack.getCount() != 1) {
            return stack;
        }
        if (!simulate) {
            inputs.set(emptySlot, stack.copyWithCount(1));
            refresh.run();
        }
        if (containerItem != Items.AIR) {
            return containerItem.getDefaultInstance();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }

    private static boolean recipeFullyMatched(RecipeItem.RecipeRecord recipe, List<ItemStack> inputs) {
        return remainingIngredients(recipe, inputs).isEmpty() && !hasUnexpectedIngredients(recipe, inputs);
    }

    private static Reference2IntMap<Item> remainingIngredients(RecipeItem.RecipeRecord recipe, List<ItemStack> inputs) {
        Reference2IntMap<Item> remaining = ingredientCounts(recipe.input());
        for (ItemStack existing : inputs) {
            if (!existing.isEmpty()) {
                Item item = existing.getItem();
                remaining.put(item, remaining.getInt(item) - 1);
            }
        }
        remaining.reference2IntEntrySet().removeIf(entry -> entry.getIntValue() <= 0);
        return remaining;
    }

    private static boolean hasUnexpectedIngredients(RecipeItem.RecipeRecord recipe, List<ItemStack> inputs) {
        Reference2IntMap<Item> expected = ingredientCounts(recipe.input());
        for (ItemStack existing : inputs) {
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

    private static Reference2IntMap<Item> ingredientCounts(List<ItemStack> inputs) {
        Reference2IntMap<Item> counts = new Reference2IntOpenHashMap<>();
        for (ItemStack input : inputs) {
            if (!input.isEmpty()) {
                Item item = input.getItem();
                counts.put(item, counts.getInt(item) + 1);
            }
        }
        return counts;
    }

    private static int firstEmptySlot(List<ItemStack> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static ItemStack insertSteamer(Level level, BlockPos pos, SteamerBlockEntity steamer,
                                           ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || isSteamerBlockedAbove(level, pos)) {
            return stack;
        }
        Optional<? extends RecipeHolder<?>> recipe = steamer.getSteamerRecipe(level, stack);
        if (recipe.isEmpty()) {
            return stack;
        }
        int emptySlotCount = steamerEmptySlotCount(steamer);
        int cookTime = steamer.getSteamerRecipe(level, stack)
                .map(holder -> holder.value().getCookTick())
                .orElse(0);
        if (emptySlotCount == 0 || cookTime <= 0) {
            return stack;
        }
        int insertCount = Math.min(stack.getCount(), emptySlotCount);
        if (!simulate) {
            NonNullList<ItemStack> items = steamer.getItems();
            int[] cookingProgress = steamer.getCookingProgress();
            int[] cookingTime = steamer.getCookingTime();
            int endIndex = steamerEndIndex(steamer);
            int left = insertCount;
            for (int i = 0; i < endIndex && left > 0; i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, stack.copyWithCount(1));
                    cookingProgress[i] = 0;
                    cookingTime[i] = cookTime;
                    left--;
                }
            }
            steamer.refresh();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(insertCount);
        return remainder;
    }

    private static int steamerEmptySlotCount(SteamerBlockEntity steamer) {
        int empty = 0;
        for (int i = 0; i < steamerEndIndex(steamer); i++) {
            if (steamer.getItems().get(i).isEmpty()) {
                empty++;
            }
        }
        return empty;
    }

    private static int steamerEndIndex(SteamerBlockEntity steamer) {
        return steamer.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
    }

    private static boolean isSteamerBlockedAbove(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, net.minecraft.core.Direction.DOWN);
    }

    private static ItemStack insertMillstone(Level level, MillstoneBlockEntity millstone,
                                             ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !millstone.getInput().isEmpty() || !millstone.isOutputEmpty()) {
            return stack;
        }
        int insertCount = Math.min(stack.getCount(), MILLSTONE_MAX_INPUT_COUNT);
        SimpleInput input = new SimpleInput(List.of(stack.copyWithCount(insertCount)));
        Optional<RecipeHolder<MillstoneRecipe>> recipe = millstone.matchRecipe(input, level);
        if (recipe.isEmpty()) {
            return stack;
        }
        if (!simulate) {
            ItemStack stackToInsert = stack.copyWithCount(insertCount);
            if (!millstone.onPutItem(level, stackToInsert)) {
                return stack;
            }
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(insertCount);
        return remainder;
    }

    private static ItemStack insertShawarmaSpit(Level level, ShawarmaSpitBlockEntity spit, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !spit.cookingItem.isEmpty() || !spit.cookedItem.isEmpty()) {
            return stack;
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level);
        if (recipe.isEmpty()) {
            return stack;
        }
        int insertedCount = Math.min(stack.getCount(), SHAWARMA_MAX_ITEMS);
        if (!simulate) {
            spit.cookingItem = stack.copyWithCount(insertedCount);
            spit.cookedItem = recipe.get().value().assemble(input, level.registryAccess());
            spit.cookedItem.setCount(insertedCount);
            spit.cookTime = recipe.get().value().getCookingTime();
            spit.refresh();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(insertedCount);
        return remainder;
    }

    private static ItemStack insertTeapot(Level level, TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (teapot.getStatus() == ITeapot.PUT_INGREDIENT) {
            if (teapot.getTeaFluidId().equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                return insertTeaFluid(teapot, stack, simulate);
            }
            if (teapot.getInput().isEmpty()) {
                ItemStack remainder = insertTeaIngredient(level, teapot, stack, simulate);
                if (remainder != stack) {
                    return remainder;
                }
            }
            return takeTeaFluid(teapot, stack, simulate);
        }
        if (teapot.getStatus() == ITeapot.FINISHED) {
            return pourTeaIntoCup(teapot, stack, simulate);
        }
        return stack;
    }

    private static ItemStack insertTeaFluid(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
        if (stack.getCount() != 1) {
            return stack;
        }
        return FluidUtil.getFluidHandler(stack.copy()).map(handler -> {
            FluidStack drained = handler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
            if (drained.getAmount() < FluidType.BUCKET_VOLUME) {
                return stack;
            }
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(drained.getFluid());
            if (fluidId == null) {
                return stack;
            }
            if (!simulate) {
                ((TeapotBlockEntityAccessor) teapot).kaleidoscopeCompat$setTeaFluidId(fluidId);
                teapot.refresh();
            }
            return handler.getContainer();
        }).orElse(stack);
    }

    private static ItemStack takeTeaFluid(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
        if (stack.getCount() != 1 || !teapot.getInput().isEmpty()
                || teapot.getTeaFluidId().equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
            return stack;
        }
        return FluidUtil.getFluidHandler(stack.copy()).map(handler -> {
            FluidStack fluid = new FluidStack(BuiltInRegistries.FLUID.get(teapot.getTeaFluidId()), FluidType.BUCKET_VOLUME);
            if (handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE) < FluidType.BUCKET_VOLUME) {
                return stack;
            }
            if (!simulate) {
                TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
                accessor.kaleidoscopeCompat$setTeaFluidId(TeapotRecipeSerializer.EMPTY_TEA_FLUID);
                accessor.kaleidoscopeCompat$setCurrentTick(-1);
                teapot.refresh();
            }
            return handler.getContainer();
        }).orElse(stack);
    }

    private static ItemStack insertTeaIngredient(Level level, TeapotBlockEntity teapot,
                                                 ItemStack stack, boolean simulate) {
        Optional<RecipeHolder<TeapotRecipe>> recipe = level.getRecipeManager().getRecipeFor(
                ModRecipes.TEAPOT_RECIPE,
                new TeapotInput(stack, teapot.getTeaFluidId()),
                level
        );
        if (recipe.isEmpty() || stack.getCount() < recipe.get().value().ingredientCount()) {
            return stack;
        }
        int count = recipe.get().value().ingredientCount();
        if (!simulate) {
            TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
            accessor.kaleidoscopeCompat$setInput(stack.copyWithCount(count));
            accessor.kaleidoscopeCompat$setCurrentTick(TeapotBlockEntity.INGREDIENT_TIME);
            teapot.refresh();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(count);
        return remainder;
    }

    private static ItemStack pourTeaIntoCup(TeapotBlockEntity teapot, ItemStack stack, boolean simulate) {
        if (!stack.is(ModItems.EMPTY_CUP.get()) || stack.isEmpty() || teapot.getResult().isEmpty()) {
            return stack;
        }
        int available = teapot.getResult().getCount();
        if (stack.getCount() > available) {
            if (simulate) {
                ItemStack remainder = stack.copy();
                remainder.shrink(available);
                return remainder;
            }
            return stack;
        }
        ItemStack filledCups = teapot.getResult().copyWithCount(stack.getCount());
        if (!simulate) {
            ItemStack remainingTea = teapot.getResult().copy();
            remainingTea.shrink(stack.getCount());
            if (remainingTea.isEmpty()) {
                resetTeapot(teapot);
            } else {
                ((TeapotBlockEntityAccessor) teapot).kaleidoscopeCompat$setResult(remainingTea);
                teapot.refresh();
            }
        }
        return filledCups;
    }

    private static void resetTeapot(TeapotBlockEntity teapot) {
        TeapotBlockEntityAccessor accessor = (TeapotBlockEntityAccessor) teapot;
        accessor.kaleidoscopeCompat$setInput(ItemStack.EMPTY);
        accessor.kaleidoscopeCompat$setTeaFluidId(TeapotRecipeSerializer.EMPTY_TEA_FLUID);
        accessor.kaleidoscopeCompat$setResult(ItemStack.EMPTY);
        accessor.kaleidoscopeCompat$setStatus(ITeapot.PUT_INGREDIENT);
        accessor.kaleidoscopeCompat$setCurrentTick(-1);
        teapot.refresh();
    }

    private static ItemStack acceptedReusableToolSignal(ItemStack stack) {
        ItemStack signal = stack.copy();
        signal.shrink(1);
        return signal;
    }
}
