package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.github.ysbbbbbb.kaleidoscopecookery.block.misc.TrashCanBlock;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TRASH_CAN_ITEMS;

/**
 * 垃圾桶在动态结构上的交互行为
 * TODO:垃圾桶交互未全部实现
 */
public class TrashCanMovingInteraction extends BaseMovingInteraction {

    private static final int SLOT_COUNT = 3;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof TrashCanBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStackHandler handler = readItems(nbt, registryAccess);

        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 空手Shift+右键取出物品
        if (mainHandItem.isEmpty() && player.isSecondaryUseActive()) {
            return withdrawItem(player, contraptionEntity, localPos, info, handler, registryAccess);
        }

        // 2. 手持物品放入
        if (!mainHandItem.isEmpty()) {
            return putItem(contraptionEntity, localPos, info, mainHandItem, handler, registryAccess);
        }

        return false;
    }

    /**
     * 放入物品到垃圾桶
     */
    private boolean putItem(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                            StructureBlockInfo info, ItemStack itemInHand,
                            ItemStackHandler handler, RegistryAccess registryAccess) {
        ItemStack toInsert = itemInHand.copy();
        ItemStack result = ItemHandlerHelper.insertItemStacked(handler, toInsert, false);

        // 如果满了，覆盖最旧物品
        if (result.getCount() == itemInHand.getCount()) {
            // 移动槽位：0←1←2
            handler.setStackInSlot(0, handler.getStackInSlot(1).copy());
            handler.setStackInSlot(1, handler.getStackInSlot(2).copy());
            handler.setStackInSlot(2, itemInHand.copy());
            itemInHand.shrink(itemInHand.getCount());
        } else {
            // 正常扣除
            itemInHand.shrink(itemInHand.getCount() - result.getCount());
        }

        if (!contraptionEntity.level().isClientSide()) {
            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            saveItems(newNbt, handler, registryAccess);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
        }

        playSound(contraptionEntity, localPos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 0.5F);
        return true;
    }

    /**
     * 从垃圾桶取出物品
     */
    private boolean withdrawItem(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                 StructureBlockInfo info, ItemStackHandler handler, RegistryAccess registryAccess) {
        // 倒序查找第一个非空槽位
        for (int i = handler.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(i, stack.getCount(), false);

                if (!contraptionEntity.level().isClientSide()) {
                    // 给玩家物品
                    player.setItemInHand(InteractionHand.MAIN_HAND, extracted.copy());

                    CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                    saveItems(newNbt, handler, registryAccess);
                    updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
                }

                playSound(contraptionEntity, localPos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 0.8F);
                return true;
            }
        }
        return false;
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
