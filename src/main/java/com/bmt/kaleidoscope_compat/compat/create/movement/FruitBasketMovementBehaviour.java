package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.FruitBasketBlock;
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

/**
 * 果篮在动态结构上的移动行为
 */
public class FruitBasketMovementBehaviour extends BaseMovementBehaviour {

    private static final int SLOT_COUNT = 8;
    private static final String BASKET_ITEMS = "BasketItems";

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof FruitBasketBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        // 果篮是存储方块，不需要tick
        return false;
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
        // 果篮无tick行为
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.world.isClientSide) return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        if (info == null || !(info.state().getBlock() instanceof FruitBasketBlock)) {
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

        // 给最近的玩家或掉落物品
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

        // 清空存储的数据
        CompoundTag newNbt = nbt.copy();
        saveItems(newNbt, handler, registryAccess);
        updateNbt(context, newNbt);
    }

    /**
     * 从 NBT 读取物品列表到 ItemStackHandler
     */
    private ItemStackHandler readItems(CompoundTag nbt, RegistryAccess registryAccess) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        if (nbt.contains(BASKET_ITEMS)) {
            CompoundTag itemsTag = nbt.getCompound(BASKET_ITEMS);
            handler.deserializeNBT(registryAccess, itemsTag);
        }
        return handler;
    }

    /**
     * 将 ItemStackHandler 保存到 NBT
     */
    private void saveItems(CompoundTag nbt, ItemStackHandler handler, RegistryAccess registryAccess) {
        nbt.put(BASKET_ITEMS, handler.serializeNBT(registryAccess));
    }
}
