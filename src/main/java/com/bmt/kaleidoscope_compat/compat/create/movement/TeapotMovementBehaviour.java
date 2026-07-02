package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSounds;
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
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TeapotStatus.*;

/**
 * 茶壶在动态结构上的移动行为
 */
public class TeapotMovementBehaviour extends BaseMovementBehaviour {

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
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        int status = nbt.getInt(STATUS);

        if (status == PUT_INGREDIENT) {
            tickPutIngredient(context, nbt);
            return;
        }

        if (status == PROCESSING) {
            tickProcessing(context, nbt);
            return;
        }

        if (status == FINISHED) {
            tickFinished(context);
        }
    }

    /**
     * 准备阶段 tick
     */
    private void tickPutIngredient(MovementContext context, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasNoHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
        if (inputTag.isEmpty()) {
            return;
        }

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt);
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
            updateNbt(context, newNbt);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.remove(TEAPOT_INPUT);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt);
    }

    /**
     * 烹饪阶段 tick
     */
    private void tickProcessing(MovementContext context, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasNoHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(STATUS, FINISHED);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt);
    }

    /**
     * 完成阶段 tick
     */
    private void tickFinished(MovementContext context) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 11) != 0) {
            return;
        }

        if (ContraptionUtil.hasHeatSource(context)) {
            onBoilingEffects(context);
        }
    }

    /**
     * 烹饪中
     */
    private void onProcessingEffects(MovementContext context) {
        playSound(context, ModSounds.BLOCK_TEAPOT_PROCESSING.get(), SoundSource.BLOCKS, 0.6f,
                0.8f + context.world.random.nextFloat() * 0.2F);
        onFinishEffects(context);
    }

    /**
     * 沸腾时
     */
    private void onBoilingEffects(MovementContext context) {
        RandomSource random = context.world.random;
        playSound(context, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.4f,
                0.8f + random.nextFloat() * 0.2F);

        if (context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + (random.nextFloat() - 0.5F),
                    gp.y + 0.1 + random.nextDouble() / 5,
                    gp.z + (random.nextFloat() - 0.5F),
                    3,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.02);
        }
    }

    private void onFinishEffects(MovementContext context) {
        RandomSource random = context.world.random;
        if (context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                    gp.y + 0.3 + random.nextDouble() / 3,
                    gp.z + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                    1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.02);
        }
    }
}
