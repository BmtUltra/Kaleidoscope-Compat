package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ChoppingBoardBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 砧板在动态结构上的移动行为
 */
public class ChoppingBoardMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof ChoppingBoardBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return false;
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.world.isClientSide) return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        if (info == null || !(info.state().getBlock() instanceof ChoppingBoardBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        RegistryAccess registryAccess = context.world.registryAccess();
        List<ItemStack> drops = new ArrayList<>();

        // 读取当前状态
        ItemStack currentCutStack = readCurrentCutStack(nbt, registryAccess);
        ItemStack resultItem = readResultItem(nbt, registryAccess);
        int currentCutCount = nbt.getInt(CHOPPING_BOARD_CURRENT_CUT_COUNT);
        int maxCutCount = nbt.getInt(CHOPPING_BOARD_MAX_CUT_COUNT);

        // 根据切制状态决定掉落什么
        if (!resultItem.isEmpty()) {
            if (currentCutCount >= maxCutCount) {
                // 已经切完，掉落成品
                drops.add(resultItem.copy());
            } else if (currentCutCount == 0 && !currentCutStack.isEmpty()) {
                // 未开始切制，掉落原料
                drops.add(currentCutStack.copy());
            }
            // 切制中的情况：掉落原料
            else if (!currentCutStack.isEmpty()) {
                drops.add(currentCutStack.copy());
            }
        } else if (!currentCutStack.isEmpty()) {
            drops.add(currentCutStack.copy());
        }

        if (!drops.isEmpty()) {
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

            if (nearestPlayer != null && closestDist < 10.0) {
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ContraptionUtil.giveItemToPlayer(nearestPlayer, drop);
                    }
                }
            } else {
                ContraptionUtil.spawnItemDrops(context.world, globalPos, drops);
            }
        }

        // 清空砧板数据
        CompoundTag newNbt = nbt.copy();
        clearBoardData(newNbt);
        updateNbt(context, newNbt);
    }

    /**
     * 清空砧板数据
     */
    private void clearBoardData(CompoundTag nbt) {
        nbt.remove(CHOPPING_BOARD_MODEL_ID);
        nbt.remove(CHOPPING_BOARD_CURRENT_CUT_STACK);
        nbt.remove(CHOPPING_BOARD_RESULT_ITEM);
        nbt.putInt(CHOPPING_BOARD_MAX_CUT_COUNT, 0);
        nbt.putInt(CHOPPING_BOARD_CURRENT_CUT_COUNT, 0);
    }

    /**
     * 读取当前切制的物品
     */
    private ItemStack readCurrentCutStack(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(CHOPPING_BOARD_CURRENT_CUT_STACK)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(CHOPPING_BOARD_CURRENT_CUT_STACK));
        }
        return ItemStack.EMPTY;
    }

    /**
     * 读取切制结果
     */
    private ItemStack readResultItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(CHOPPING_BOARD_RESULT_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(CHOPPING_BOARD_RESULT_ITEM));
        }
        return ItemStack.EMPTY;
    }
}
