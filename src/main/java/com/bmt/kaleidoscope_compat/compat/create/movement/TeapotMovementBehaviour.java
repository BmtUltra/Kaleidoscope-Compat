package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * 茶壶在动态结构上的移动行为
 */
public class TeapotMovementBehaviour extends BaseMovementBehaviour {

    private static final String TEA_FLUID_ID = "TeaFluidId";
    private static final String RESULT = "Result";
    private static final String STATUS = "Status";
    private static final String INPUT = "Input";
    private static final String CURRENT_TICK = "CurrentTick";

    /**
     * 状态常量
     */
    private static final int PUT_INGREDIENT = 0;
    private static final int PROCESSING = 1;
    private static final int FINISHED = 2;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof TeapotBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        String teaFluidId = nbt.getString(TEA_FLUID_ID);
        return !teaFluidId.isEmpty() && !teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString());
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
        int status = nbt.getInt(STATUS);

        if (status == PUT_INGREDIENT) {
            tickPutIngredient(context, nbt);
            return;
        }

        if (status == PROCESSING) {
            tickProcessing(context, state, nbt);
            return;
        }

        if (status == FINISHED) {
            tickFinished(context, state, nbt);
        }
    }

    /**
     * PUT_INGREDIENT 阶段 tick
     */
    private void tickPutIngredient(MovementContext context, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        CompoundTag inputTag = nbt.getCompound(INPUT);
        if (inputTag.isEmpty()) {
            return;
        }

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt, true);
            return;
        }

        ItemStack input = ItemStack.parseOptional(context.world.registryAccess(), inputTag);
        String teaFluidId = nbt.getString(TEA_FLUID_ID);

        TeapotInput container = new TeapotInput(input, ResourceLocation.parse(teaFluidId));
        Optional<RecipeHolder<TeapotRecipe>> recipeOpt = context.world.getRecipeManager()
                .getRecipeFor(ModRecipes.TEAPOT_RECIPE, container, context.world);

        if (recipeOpt.isPresent()) {
            TeapotRecipe teapotRecipe = recipeOpt.get().value();
            ItemStack result = teapotRecipe.assemble(container, context.world.registryAccess());

            CompoundTag newNbt = nbt.copy();
            newNbt.put(RESULT, result.saveOptional(context.world.registryAccess()));
            newNbt.putInt(CURRENT_TICK, teapotRecipe.time());
            newNbt.putInt(STATUS, PROCESSING);
            updateNbt(context, newNbt, true);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.remove(INPUT);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt, true);
    }

    /**
     * PROCESSING 阶段 tick
     */
    private void tickProcessing(MovementContext context, BlockState state, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt, true);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(STATUS, FINISHED);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt, true);
    }

    /**
     * FINISHED 阶段 tick
     */
    private void tickFinished(MovementContext context, BlockState state, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 11) != 0) {
            return;
        }

        if (hasHeatSource(context)) {
            onFinishEffects(context);
        } else {
            onBoilingEffects(context);
        }
    }

    /**
     * 烹饪中的音效和粒子
     */
    private void onProcessingEffects(MovementContext context) {
        playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f,
                0.8f + context.world.random.nextFloat() * 0.2F);
        onFinishEffects(context);
    }

    /**
     * 沸腾时的粒子效果
     */
    private void onBoilingEffects(MovementContext context) {
        if (!(context.world instanceof ServerLevel sl)) return;

        RandomSource random = sl.random;
        playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f,
                0.8f + random.nextFloat() * 0.2F);

        var gp = getGlobalPos(context);
        sl.sendParticles(ModParticles.COOKING.get(),
                gp.x + 0.5 + (random.nextFloat() - 0.5F),
                gp.y + 0.6 + random.nextDouble() / 5,
                gp.z + 0.5 + (random.nextFloat() - 0.5F),
                3,
                (random.nextFloat() - 0.5) * 0.05F,
                0.1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.02);
    }

    /**
     * 完成时的粒子效果
     */
    private void onFinishEffects(MovementContext context) {
        if (!(context.world instanceof ServerLevel sl)) return;

        RandomSource random = sl.random;
        var gp = getGlobalPos(context);
        sl.sendParticles(ModParticles.COOKING.get(),
                gp.x + 0.5 + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                gp.y + 0.8 + random.nextDouble() / 3,
                gp.z + 0.5 + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.02);
    }
}
