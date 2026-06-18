package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.misc.TrashCanBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TRASH_CAN_ITEMS;

/**
 * 垃圾桶在动态结构上的移动行为
 * TODO:实现跳入垃圾桶的效果
 */
public class TrashCanMovementBehaviour extends BaseMovementBehaviour {

    private static final int SLOT_COUNT = 3;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof TrashCanBlock;
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
        if (info == null || !(info.state().getBlock() instanceof TrashCanBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        RegistryAccess registryAccess = context.world.registryAccess();
        ItemStackHandler handler = readItems(nbt, registryAccess);

        // 收集所有物品
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
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
        }

        // 清空存储的数据
        CompoundTag newNbt = nbt.copy();
        saveItems(newNbt, handler, registryAccess);
        updateNbt(context, newNbt);
    }

    /**
     * 从 NBT 读取物品列表
     */
    private ItemStackHandler readItems(CompoundTag nbt, RegistryAccess registryAccess) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        if (nbt.contains(TRASH_CAN_ITEMS)) {
            CompoundTag itemsTag = nbt.getCompound(TRASH_CAN_ITEMS);
            handler.deserializeNBT(registryAccess, itemsTag);
        }
        return handler;
    }

    /**
     * 保存物品列表到 NBT
     */
    private void saveItems(CompoundTag nbt, ItemStackHandler handler, RegistryAccess registryAccess) {
        nbt.put(TRASH_CAN_ITEMS, handler.serializeNBT(registryAccess));
    }
}
