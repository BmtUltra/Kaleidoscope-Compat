package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.OIL_POT_OIL_COUNT;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock.HAS_OIL;

/**
 * 油壶在动态结构上的移动行为
 */
public class OilPotMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof OilPotBlock;
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
        if (info == null || !(info.state().getBlock() instanceof OilPotBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        int oilCount = nbt.getInt(OIL_POT_OIL_COUNT);
        if (oilCount <= 0) {
            // 清空数据并更新BlockState
            BlockState newState = info.state().setValue(HAS_OIL, false);
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(OIL_POT_OIL_COUNT, 0);
            updateData(context, newState, newNbt);
            return;
        }

        // 计算全局位置
        Vec3 globalPos = context.contraption.entity.toGlobalVector(
                Vec3.atCenterOf(context.localPos), 1.0f);

        // 掉落油脂
        List<ItemStack> drops = new ArrayList<>();
        int remaining = oilCount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            drops.add(new ItemStack(ModItems.OIL.get(), stackSize));
            remaining -= stackSize;
        }

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
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    ContraptionUtil.giveItemToPlayer(nearestPlayer, drop);
                }
            }
        } else {
            ContraptionUtil.spawnItemDrops(context.world, globalPos, drops);
        }

        // 清空油量并更新BlockState
        BlockState newState = info.state().setValue(HAS_OIL, false);
        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(OIL_POT_OIL_COUNT, 0);
        updateData(context, newState, newNbt);
    }
}
