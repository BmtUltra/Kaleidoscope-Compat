package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.KitchenwareRacksBlock;
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

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.KITCHENWARE_RACKS_LEFT_ITEM;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.KITCHENWARE_RACKS_RIGHT_ITEM;

/**
 * 厨具架在动态结构上的移动行为
 */
public class KitchenwareRacksMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof KitchenwareRacksBlock;
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
        if (info == null || !(info.state().getBlock() instanceof KitchenwareRacksBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        RegistryAccess registryAccess = context.world.registryAccess();

        // 收集物品
        List<ItemStack> drops = new ArrayList<>();
        ItemStack itemLeft = readItem(nbt, KITCHENWARE_RACKS_LEFT_ITEM, registryAccess);
        ItemStack itemRight = readItem(nbt, KITCHENWARE_RACKS_RIGHT_ITEM, registryAccess);

        if (!itemLeft.isEmpty()) {
            drops.add(itemLeft.copy());
        }
        if (!itemRight.isEmpty()) {
            drops.add(itemRight.copy());
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

        // 清空数据
        CompoundTag newNbt = nbt.copy();
        newNbt.put(KITCHENWARE_RACKS_LEFT_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
        newNbt.put(KITCHENWARE_RACKS_RIGHT_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
        updateNbt(context, newNbt);
    }

    private ItemStack readItem(CompoundTag nbt, String key, RegistryAccess registryAccess) {
        if (nbt.contains(key)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(key));
        }
        return ItemStack.EMPTY;
    }
}
