package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.PotStatus.*;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.SHOW_OIL;
import static com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry.SUSPICIOUS_STIR_FRY;
import static com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry.getItem;

/**
 * 炒锅在动态结构上的移动行为
 */
public class PotMovementBehaviour extends BaseMovementBehaviour {

    private static final int TAKEOUT_TIME = 40 * 20;
    private static final int BURNT_TIME = 20 * 20;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof PotBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return state.getValue(HAS_OIL);
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (hasNoHeatSource(context)) {
            return;
        }

        int currentTick = nbt.getInt(CURRENT_TICK);
        int status = nbt.getInt(STATUS);
        RandomSource random = context.world.random;
        boolean statusChanged = false;

        if (currentTick > 0) {
            currentTick--;
            if (currentTick % 5 == 0) {
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(CURRENT_TICK, currentTick);
                updateNbt(context, newNbt);
            }
            if (currentTick % 20 == 0) {
                playSound(context, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5f + random.nextFloat() / 0.5f, 0.8f + random.nextFloat() / 0.5f);
            }
        }

        switch (status) {
            case PUT_INGREDIENT -> statusChanged = tickPutIngredient(context, state, nbt, currentTick, random);
            case COOKING -> statusChanged = tickCooking(context, state, nbt, currentTick);
            case FINISHED -> statusChanged = tickFinished(context, state, nbt, currentTick, random);
            case BURNT -> statusChanged = tickBurnt(context, state, nbt, currentTick, random);
        }

        if (statusChanged) {
            var updatedInfo = context.contraption.getBlocks().get(context.localPos);
            if (updatedInfo != null && updatedInfo.nbt() != null) {
                int newStatus = updatedInfo.nbt().getInt(STATUS);
                if (newStatus != status) {
                    if (context.contraption.entity != null) {
                        ContraptionUtil.updateContraptionData(context.contraption.entity, context.localPos, updatedInfo);
                    }
                }
            }
        }
    }

    private boolean tickPutIngredient(MovementContext context, BlockState state, CompoundTag nbt,
                                      int currentTick, RandomSource random) {
        if (currentTick % 10 == 0 && context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + random.nextDouble() / 5 * (random.nextBoolean() ? 1 : -1),
                    gp.y - 0.4 + random.nextDouble() / 3,
                    gp.z + random.nextDouble() / 5 * (random.nextBoolean() ? 1 : -1),
                    1, 0, 0, 0, 0);
        }

        if (currentTick == 0) {
            if (ContraptionUtil.areInputsEmpty(nbt, context.world.registryAccess(), PotRecipe.RECIPES_SIZE)) {
                resetPot(context, state);
                playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
            } else {
                startCooking(context, state, nbt);
            }
            return true;
        } else {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, currentTick);
            updateNbt(context, newNbt);
            return false;
        }
    }

    private boolean tickCooking(MovementContext context, BlockState state, CompoundTag nbt,
                                int currentTick) {
        if (currentTick == 0) {
            playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);

            RegistryAccess registryAccess = context.world.registryAccess();
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(STATUS, FINISHED);

            int stirFryCount = newNbt.getInt(POT_STIR_FRY_COUNT);
            if (stirFryCount > 0) {
                ContraptionUtil.saveCarrier(newNbt, Ingredient.of(Items.BOWL), CARRIER);
                newNbt.put(RESULT, new ItemStack(getItem(SUSPICIOUS_STIR_FRY)).saveOptional(registryAccess));
            }

            newNbt.putInt(CURRENT_TICK, TAKEOUT_TIME);
            BlockState newState = state.setValue(SHOW_OIL, false);
            updateData(context, newState, newNbt);
            return true;
        } else {
            CompoundTag newNbt = nbt.copy();                                                
            newNbt.putInt(CURRENT_TICK, currentTick);
            updateNbt(context, newNbt);
            return false;
        }
    }

    private boolean tickFinished(MovementContext context, BlockState state, CompoundTag nbt,
                                 int currentTick, RandomSource random) {
        if (currentTick % 10 == 0 && context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + random.nextDouble() / 5 * (random.nextBoolean() ? 1 : -1),
                    gp.y - 0.4 + random.nextDouble() / 2,
                    gp.z + random.nextDouble() / 5 * (random.nextBoolean() ? 1 : -1),
                    1, 0, 0, 0, 0);
        }

        CompoundTag newNbt = nbt.copy();
        if (currentTick == 0) {
            newNbt.putInt(STATUS, BURNT);
            newNbt.putInt(CURRENT_TICK, BURNT_TIME);
            updateData(context, state, newNbt);
            return true;
        } else {
            newNbt.putInt(CURRENT_TICK, currentTick);
            updateNbt(context, newNbt);
            return false;
        }
    }

    private boolean tickBurnt(MovementContext context, BlockState state, CompoundTag nbt,
                              int currentTick, RandomSource random) {
        int particleCount = 10 - currentTick / 5;
        if (currentTick % 2 == 0 && context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ParticleTypes.SMOKE,
                    gp.x + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    gp.y - 0.25 + random.nextDouble() / 3,
                    gp.z + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    particleCount, 0, 0, 0, 0.05);
        }

        if (currentTick == 0) {
            resetPot(context, state);
            playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
            if (context.world instanceof ServerLevel sl) {
                Vec3 gp = getGlobalPos(context);
                sl.sendParticles(ParticleTypes.SMOKE,
                        gp.x + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                        gp.y - 0.25 + random.nextDouble() / 3,
                        gp.z + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                        8, 0, 0, 0, 0.05);
                int count = 1 + random.nextInt(3);
                Block.popResource(context.world, BlockPos.containing(gp),
                        new ItemStack(Items.CHARCOAL, count));
            }
            return true;
        } else {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, currentTick);
            updateNbt(context, newNbt);
            return false;
        }
    }

    private void startCooking(MovementContext context, BlockState state, CompoundTag nbt) {
        RegistryAccess registryAccess = context.world.registryAccess();
        NonNullList<ItemStack> inputs = ContraptionUtil.readInputs(nbt, registryAccess, PotRecipe.RECIPES_SIZE);
        SimpleInput input = new SimpleInput(inputs);

        var recipeOpt = context.world.getRecipeManager().getRecipeFor(ModRecipes.POT_RECIPE, input, context.world);

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(STATUS, COOKING);
        // 设置 seed 用于客户端翻动动画
        newNbt.putLong(SEED, System.currentTimeMillis());

        recipeOpt.ifPresentOrElse(holder -> {
            PotRecipe recipe = holder.value();
            ContraptionUtil.saveCarrier(newNbt, recipe.carrier(), CARRIER);
            newNbt.put(RESULT, recipe.assemble(input, registryAccess).saveOptional(registryAccess));
            newNbt.putInt(CURRENT_TICK, recipe.time());
            newNbt.putInt(POT_STIR_FRY_COUNT, recipe.stirFryCount());
        }, () -> {
            ContraptionUtil.saveCarrier(newNbt, Ingredient.of(Items.BOWL), CARRIER);
            newNbt.put(RESULT, new ItemStack(getItem(SUSPICIOUS_STIR_FRY)).saveOptional(registryAccess));
            newNbt.putInt(CURRENT_TICK, 10 * 20);
            newNbt.putInt(POT_STIR_FRY_COUNT, 0);
        });

        updateData(context, state, newNbt);
    }

    private void resetPot(MovementContext context, BlockState state) {
        RegistryAccess registryAccess = context.world.registryAccess();
        CompoundTag newNbt = new CompoundTag();
        ContraptionUtil.saveInputs(newNbt, NonNullList.withSize(PotRecipe.RECIPES_SIZE, ItemStack.EMPTY), registryAccess);
        ContraptionUtil.saveCarrier(newNbt, Ingredient.EMPTY, CARRIER);
        newNbt.put(RESULT, ItemStack.EMPTY.saveOptional(registryAccess));
        newNbt.putInt(STATUS, PUT_INGREDIENT);
        newNbt.putInt(CURRENT_TICK, 0);
        newNbt.putInt(POT_STIR_FRY_COUNT, 0);

        BlockState newState = state.setValue(HAS_OIL, false);
        updateData(context, newState, newNbt);
    }
}
