package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 烤肉架在动态结构上的移动行为
 * TODO:修复烤肉架会在拆卸结构时消失的问题
 */
public class ShawarmaSpitMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof ShawarmaSpitBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        int cookTime = nbt.getInt(SHAWARMA_SPIT_COOK_TIME);
        return cookTime > 0;
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
        int cookTime = nbt.getInt(SHAWARMA_SPIT_COOK_TIME);

        if (cookTime > 0) {
            cookTime--;

            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(SHAWARMA_SPIT_COOK_TIME, cookTime);

            // 每10tick保存一次
            if (cookTime % 10 == 0) {
                updateNbt(context, newNbt);
            }

            // 生成烹饪粒子
            if (context.world.random.nextFloat() < 0.25f) {
                ContraptionUtil.spawnParticle(context, ModParticles.COOKING.get(),
                        0, 0.5, 0, 1, 0.25, 0.2, 0.25, 0.1);
            }

            // 烹饪完成时播放音效
            if (cookTime == 0) {
                playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                        0.5F + context.world.random.nextFloat(),
                        0.6F + context.world.random.nextFloat() * 0.7F);
            }
        }
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.world.isClientSide) return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        if (info == null || !(info.state().getBlock() instanceof ShawarmaSpitBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        RegistryAccess registryAccess = context.world.registryAccess();

        // 读取状态
        ItemStack cookingItem = readCookingItem(nbt, registryAccess);
        ItemStack cookedItem = readCookedItem(nbt, registryAccess);
        int cookTime = nbt.getInt(SHAWARMA_SPIT_COOK_TIME);

        // 决定掉落什么
        ItemStack drop = ItemStack.EMPTY;
        if (cookTime <= 0 && !cookedItem.isEmpty()) {
            // 烹饪完成，掉落成品
            drop = cookedItem.copy();
        } else if (!cookingItem.isEmpty()) {
            // 还在烹饪或未完成，掉落原料
            drop = cookingItem.copy();
        }

        if (!drop.isEmpty()) {
            // 计算全局位置
            Vec3 globalPos = context.contraption.entity.toGlobalVector(
                    Vec3.atCenterOf(context.localPos), 1.0f);

            // 查找最近的玩家
            Player nearestPlayer = null;
            double closestDist = Double.MAX_VALUE;
            for (Player player : context.world.players()) {
                double dist = player.position().distanceTo(globalPos);
                if (dist < closestDist) {
                    closestDist = dist;
                    nearestPlayer = player;
                }
            }

            // 给最近的玩家或掉落物品
            if (nearestPlayer != null && closestDist < 10.0) {
                ContraptionUtil.giveItemToPlayer(nearestPlayer, drop);
            } else {
                List<ItemStack> drops = new ArrayList<>();
                drops.add(drop);
                ContraptionUtil.spawnItemDrops(context.world, globalPos, drops);
            }
        }

        // 清空数据
        CompoundTag newNbt = nbt.copy();
        newNbt.remove(SHAWARMA_SPIT_COOKING_ITEM);
        newNbt.remove(SHAWARMA_SPIT_COOKED_ITEM);
        newNbt.putInt(SHAWARMA_SPIT_COOK_TIME, 0);
        updateNbt(context, newNbt);
    }

    private ItemStack readCookingItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(SHAWARMA_SPIT_COOKING_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(SHAWARMA_SPIT_COOKING_ITEM));
        }
        return ItemStack.EMPTY;
    }

    private ItemStack readCookedItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(SHAWARMA_SPIT_COOKED_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(SHAWARMA_SPIT_COOKED_ITEM));
        }
        return ItemStack.EMPTY;
    }
}
