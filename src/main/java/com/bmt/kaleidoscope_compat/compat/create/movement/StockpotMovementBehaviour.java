package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.client.particle.StockpotParticleOptions;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.FlexStockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotVisuals;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.Quality;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityEvaluator;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityUtils;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.StockpotStatus.*;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock.HAS_LID;

/**
 * 汤锅在动态结构上的移动行为
 */
public class StockpotMovementBehaviour extends BaseMovementBehaviour {

    private static final int MAX_TAKEOUT_COUNT = 9;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof StockpotBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        int status = nbt.getInt(STATUS);
        return status != PUT_SOUP_BASE;
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (hasNoHeatSource(context)) {
            return;
        }

        int status = nbt.getInt(STATUS);
        boolean hasLid = state.getValue(HAS_LID);
        RandomSource random = context.world.random;

        if (context.world.getGameTime() % 15 == 0) {
            float volume = hasLid ? 0.075f : 0.2f;
            float pitch = hasLid ? 0.1f + random.nextFloat() * 0.05f : 1f + random.nextFloat() * 0.1f;
            playSound(context, ModSounds.BLOCK_STOCKPOT.get(), SoundSource.BLOCKS, volume, pitch);
        }

        if (!hasLid) {
            spawnParticleWithoutLid(context, nbt, random);
            return;
        }

        if (random.nextFloat() < 0.05F && context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    gp.y - 0.125 + random.nextDouble() / 3,
                    gp.z + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    1, 0, 0, 0, 0.05);
        }

        if (status == PUT_INGREDIENT && context.world.getGameTime() % 5 == 0 && !ContraptionUtil.areInputsEmpty(nbt, context.world.registryAccess(), StockpotRecipe.RECIPES_SIZE)) {
            matchRecipe(context, state, nbt);
            return;
        }

        if (status == COOKING) {
            int currentTick = nbt.getInt(CURRENT_TICK);
            if (currentTick > 0) {
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(CURRENT_TICK, currentTick - 1);
                updateNbt(context, newNbt);
                return;
            }
            finishCooking(context, state, nbt);
        }
    }

    private void spawnParticleWithoutLid(MovementContext context, CompoundTag nbt, RandomSource random) {
        if (random.nextFloat() >= 0.25F) return;
        if (!(context.world instanceof ServerLevel sl)) return;

        int color = getBubbleColor(nbt, context.world);
        Vec3 gp = getGlobalPos(context);
        sl.sendParticles(new StockpotParticleOptions(Vec3.fromRGB24(color).toVector3f(), 1f),
                gp.x - 0.25 + (random.nextFloat() * 0.5F),
                gp.y - 0.125,
                gp.z - 0.25 + (random.nextFloat() * 0.5F),
                2,
                (random.nextFloat() - 0.5) * 0.1F,
                0,
                (random.nextFloat() - 0.5) * 0.1F,
                0);
    }

    private int getBubbleColor(CompoundTag nbt, Level level) {
        int status = nbt.getInt(STATUS);
        String recipeIdStr = nbt.getString(STOCKPOT_RECIPE_ID);

        if (!recipeIdStr.isEmpty()) {
            ResourceLocation recipeId = ResourceLocation.tryParse(recipeIdStr);
            if (recipeId != null) {
                var stockpotRecipe = level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE, recipeId);
                if (stockpotRecipe != null) {
                    StockpotVisuals visuals = stockpotRecipe.value().visuals();
                    return status == COOKING ? visuals.cookingBubbleColor() : visuals.finishedBubbleColor();
                }

                var flexRecipe = level.getRecipeManager().byKeyTyped(ModRecipes.FLEX_STOCKPOT_RECIPE, recipeId);
                if (flexRecipe != null) {
                    StockpotVisuals visuals = flexRecipe.value().visuals();
                    return status == COOKING ? visuals.cookingBubbleColor() : visuals.finishedBubbleColor();
                }
            }
        }

        ResourceLocation soupBaseId = ResourceLocation.tryParse(nbt.getString(STOCKPOT_SOUP_BASE_ID));
        if (soupBaseId != null) {
            ISoupBase soup = SoupBaseManager.getSoupBase(soupBaseId);
            if (soup != null) {
                return soup.getBubbleColor();
            }
        }

        return status == COOKING ? StockpotVisuals.DEFAULT_COOKING_BUBBLE_COLOR : StockpotVisuals.DEFAULT_FINISHED_BUBBLE_COLOR;
    }

    private void matchRecipe(MovementContext context, BlockState state, CompoundTag nbt) {
        RegistryAccess registryAccess = context.world.registryAccess();
        NonNullList<ItemStack> inputs = ContraptionUtil.readInputs(nbt, registryAccess, StockpotRecipe.RECIPES_SIZE);
        ResourceLocation soupBaseId = ResourceLocation.tryParse(nbt.getString(ContraptionNbtKeys.STOCKPOT_SOUP_BASE_ID));
        StockpotInput input = new StockpotInput(inputs, soupBaseId != null ? soupBaseId : ModSoupBases.WATER);

        CompoundTag newNbt = nbt.copy();

        var stockpotRecipe = context.world.getRecipeManager().getRecipeFor(ModRecipes.STOCKPOT_RECIPE, input, context.world);
        if (stockpotRecipe.isPresent()) {
            applyRecipe(context, state, newNbt, input, stockpotRecipe.get(), registryAccess);
            return;
        }

        var flexRecipe = context.world.getRecipeManager().getRecipeFor(ModRecipes.FLEX_STOCKPOT_RECIPE, input, context.world);
        if (flexRecipe.isPresent()) {
            applyFlexRecipe(context, state, newNbt, input, flexRecipe.get(), registryAccess);
            return;
        }

        applySuspiciousRecipe(context, state, newNbt, registryAccess);
    }

    private void applyRecipe(MovementContext context, BlockState state, CompoundTag newNbt,
                             StockpotInput input, RecipeHolder<StockpotRecipe> recipe,
                             RegistryAccess registryAccess) {
        StockpotRecipe value = recipe.value();
        ItemStack resultItem = value.assemble(input, registryAccess);

        newNbt.putString(ContraptionNbtKeys.STOCKPOT_RECIPE_ID, recipe.id().toString());
        ContraptionUtil.saveCarrier(newNbt, value.carrier(), ContraptionNbtKeys.STOCKPOT_CARRIER);
        newNbt.put(ContraptionNbtKeys.RESULT, resultItem.saveOptional(registryAccess));
        newNbt.putInt(ContraptionNbtKeys.CURRENT_TICK, value.time());
        newNbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.StockpotStatus.COOKING);
        newNbt.putInt(ContraptionNbtKeys.STOCKPOT_TAKEOUT_COUNT, Math.min(resultItem.getCount(), MAX_TAKEOUT_COUNT));

        updateData(context, state, newNbt);
    }

    private void applyFlexRecipe(MovementContext context, BlockState state, CompoundTag newNbt,
                                 StockpotInput input, RecipeHolder<FlexStockpotRecipe> recipe,
                                 RegistryAccess registryAccess) {
        FlexStockpotRecipe value = recipe.value();
        NonNullList<ItemStack> inputs = ContraptionUtil.readInputs(newNbt, registryAccess, StockpotRecipe.RECIPES_SIZE);
        ItemStack resultItem = value.assemble(input, registryAccess);

        if (context.world instanceof ServerLevel sl) {
            Quality quality = QualityEvaluator.evaluate(inputs, value.ingredients(), recipe.id(), sl.getSeed());
            QualityUtils.setQuality(resultItem, quality);
        }

        newNbt.putString(ContraptionNbtKeys.STOCKPOT_RECIPE_ID, recipe.id().toString());
        ContraptionUtil.saveCarrier(newNbt, value.carrier(), ContraptionNbtKeys.STOCKPOT_CARRIER);
        newNbt.put(ContraptionNbtKeys.RESULT, resultItem.saveOptional(registryAccess));
        newNbt.putInt(ContraptionNbtKeys.CURRENT_TICK, value.time());
        newNbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.StockpotStatus.COOKING);
        newNbt.putInt(ContraptionNbtKeys.STOCKPOT_TAKEOUT_COUNT, Math.min(resultItem.getCount(), MAX_TAKEOUT_COUNT));

        updateData(context, state, newNbt);
    }

    private void applySuspiciousRecipe(MovementContext context, BlockState state, CompoundTag newNbt, RegistryAccess registryAccess) {
        newNbt.putString(ContraptionNbtKeys.STOCKPOT_RECIPE_ID, "");
        ContraptionUtil.saveCarrier(newNbt, Ingredient.of(Items.BOWL), ContraptionNbtKeys.STOCKPOT_CARRIER);
        newNbt.put(ContraptionNbtKeys.RESULT, Items.SUSPICIOUS_STEW.getDefaultInstance().saveOptional(registryAccess));
        newNbt.putInt(ContraptionNbtKeys.CURRENT_TICK, 20 * 20);
        newNbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.StockpotStatus.COOKING);
        newNbt.putInt(ContraptionNbtKeys.STOCKPOT_TAKEOUT_COUNT, 1);

        updateData(context, state, newNbt);
    }

    private void finishCooking(MovementContext context, BlockState state, CompoundTag nbt) {
        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.StockpotStatus.FINISHED);
        newNbt.putInt(ContraptionNbtKeys.CURRENT_TICK, -1);
        ContraptionUtil.saveInputs(newNbt, NonNullList.withSize(StockpotRecipe.RECIPES_SIZE, ItemStack.EMPTY), context.world.registryAccess());

        updateData(context, state, newNbt);
    }
}
