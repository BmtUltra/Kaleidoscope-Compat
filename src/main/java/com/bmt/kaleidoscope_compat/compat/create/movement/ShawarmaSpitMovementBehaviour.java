package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 烤肉架在动态结构上的移动行为
 */
public class ShawarmaSpitMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof ShawarmaSpitBlock;
    }

    /**
     * 只要烤肉架上有物品就需要 tick
     */
    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (nbt == null) return false;
        var access = context.world.registryAccess();
        ItemStack cookingItem = ItemStack.parseOptional(access, nbt.getCompound(SHAWARMA_SPIT_COOKING_ITEM));
        ItemStack cookedItem = ItemStack.parseOptional(access, nbt.getCompound(SHAWARMA_SPIT_COOKED_ITEM));
        return !cookingItem.isEmpty() || !cookedItem.isEmpty();
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        boolean powered = hasRedstonePower(state);

        ItemStack cookingItem = ItemStack.parseOptional(
                context.world.registryAccess(),
                nbt.getCompound(SHAWARMA_SPIT_COOKING_ITEM));
        ItemStack cookedItem = ItemStack.parseOptional(
                context.world.registryAccess(),
                nbt.getCompound(SHAWARMA_SPIT_COOKED_ITEM));
        int cookTime = nbt.getInt(SHAWARMA_SPIT_COOK_TIME);

        boolean needsSave = false;
        CompoundTag newNbt = nbt.copy();

        if (!cookingItem.isEmpty()) {
            // 正在烹饪中：红石激活时递减 cookTime
            if (powered) {
                spawnCookingParticles(context);

                if (context.world.random.nextInt(20) == 0) {
                    playSound(context, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                            0.5F + context.world.random.nextFloat(),
                            0.6F + context.world.random.nextFloat() * 0.7F);
                }

                if (cookTime > 0) {
                    cookTime--;
                    newNbt.putInt(SHAWARMA_SPIT_COOK_TIME, cookTime);
                    needsSave = true;
                }

                // 烹饪完成：清除 cookingItem，保留 cookedItem 作为成品展示
                if (cookTime == 0) {
                    newNbt.put(SHAWARMA_SPIT_COOKING_ITEM, ItemStack.EMPTY.saveOptional(context.world.registryAccess()));
                    playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                            0.5F + context.world.random.nextFloat(),
                            0.6F + context.world.random.nextFloat() * 0.7F);
                    needsSave = true;
                }
            }
        } else if (!cookedItem.isEmpty() && powered) {
            spawnCookingParticles(context);
        }

        if (needsSave) {
            updateNbt(context, newNbt);
        }
    }

    private void spawnCookingParticles(MovementContext context) {
        if (context.world instanceof ServerLevel sl && context.world.random.nextFloat() < 0.25f) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x, gp.y, gp.z, 1, 0.25, 0.2, 0.25, 0.1);
        }
    }
}
