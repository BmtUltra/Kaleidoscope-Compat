package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.FruitBasketBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
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
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * 果篮在动态结构上的交互行为
 */
public class FruitBasketMovingInteraction extends BaseMovingInteraction {

    private static final int SLOT_COUNT = 8;
    private static final String BASKET_ITEMS = "BasketItems";

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        // 副手不处理
        if (activeHand == InteractionHand.OFF_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof FruitBasketBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStackHandler handler = readItems(nbt, registryAccess);

        if (player.isSecondaryUseActive()) {
            // Shift+右键：取出物品
            return takeOut(player, contraptionEntity, localPos, info, handler, registryAccess);
        } else {
            // 右键：放入物品
            return putIn(player, contraptionEntity, localPos, info, handler, registryAccess);
        }
    }

    /**
     * 放入物品到果篮
     */
    private boolean putIn(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                          StructureBlockInfo info, ItemStackHandler handler, RegistryAccess registryAccess) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.isEmpty() || mainHandItem.is(ModItems.TRANSMUTATION_LUNCH_BAG.get())) {
            return false;
        }

        // 检查物品是否可以放入容器物品中
        if (!mainHandItem.getItem().canFitInsideContainerItems()) {
            return false;
        }

        ItemStack toInsert = mainHandItem.copy();
        ItemStack remainder = insertItemStacked(handler, toInsert);

        int inserted = toInsert.getCount() - remainder.getCount();
        if (inserted > 0) {
            mainHandItem.shrink(inserted);

            if (!contraptionEntity.level().isClientSide()) {
                CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                saveItems(newNbt, handler, registryAccess);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    /**
     * 从果篮取出物品
     */
    private boolean takeOut(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                            StructureBlockInfo info, ItemStackHandler handler, RegistryAccess registryAccess) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(i, stack.getCount(), false);

                if (!contraptionEntity.level().isClientSide()) {
                    ContraptionUtil.giveItemToPlayer(player, extracted);
                    CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                    saveItems(newNbt, handler, registryAccess);
                    updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
                }

                playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }

        return false;
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

    /**
     * 返回剩余物品
     */
    private ItemStack insertItemStacked(ItemStackHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                remaining = handler.insertItem(i, remaining, false);
            }
        }

        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                remaining = handler.insertItem(i, remaining, false);
            }
        }

        return remaining;
    }
}
