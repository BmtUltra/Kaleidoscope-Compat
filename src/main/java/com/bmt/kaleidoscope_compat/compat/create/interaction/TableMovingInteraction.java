package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.TableBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.util.BlockDrop;
import com.github.ysbbbbbb.kaleidoscopecookery.util.CarpetColor;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TABLE_CARPET_COLOR;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TABLE_ITEMS;

/**
 * 桌子在动态结构上的交互行为
 */
public class TableMovingInteraction extends BaseMovingInteraction {

    private static final int SLOT_COUNT = 4;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (player.isSecondaryUseActive()) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof TableBlock)) {
            return false;
        }

        BlockState state = info.state();
        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 手持地毯 → 放置/替换地毯
        if (mainHandItem.is(ItemTags.WOOL_CARPETS)) {
            return handleCarpet(contraptionEntity, localPos, info, state, mainHandItem);
        }

        // 2. 物品存取
        CompoundTag nbt = getOrCreateNbt(info);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStackHandler handler = readItems(nbt, registryAccess);

        return handleItems(player, contraptionEntity, localPos, info, state, handler, mainHandItem, registryAccess);
    }

    /**
     * 处理物品放入/取出
     */
    private boolean handleItems(Player player, AbstractContraptionEntity contraptionEntity,
                                 BlockPos localPos, StructureBlockInfo info, BlockState state,
                                 ItemStackHandler handler, ItemStack mainHandItem,
                                 RegistryAccess registryAccess) {
        Pair<Integer, ItemStack> lastStack = ItemUtils.getLastStack(handler);
        Integer tableIndex = lastStack.getLeft();
        ItemStack tableItem = lastStack.getRight();

        boolean handEmpty = mainHandItem.isEmpty();

        // 玩家手为空，桌子有物品：取出桌子物品
        if (handEmpty && !tableItem.isEmpty()) {
            if (!contraptionEntity.level().isClientSide()) {
                ContraptionUtil.giveItemToPlayer(player, tableItem.copy());
                handler.setStackInSlot(tableIndex, ItemStack.EMPTY);

                CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                saveItems(newNbt, handler, registryAccess);
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), info.state(), newNbt));
            }
            playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // 玩家手有物品，并且可以放入物品时
        if (!handEmpty && tableIndex < (handler.getSlots() - 1)) {
            ItemStack split = mainHandItem.split(1);
            if (tableItem.isEmpty()) {
                handler.setStackInSlot(tableIndex, split);
            } else {
                handler.setStackInSlot(tableIndex + 1, split);
            }

            if (!contraptionEntity.level().isClientSide()) {
                CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                saveItems(newNbt, handler, registryAccess);
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), info.state(), newNbt));
            }
            playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    /**
     * 处理地毯放置/替换
     */
    private boolean handleCarpet(AbstractContraptionEntity contraptionEntity,
                                 BlockPos localPos, StructureBlockInfo info, BlockState state,
                                 ItemStack itemInHand) {
        @Nullable DyeColor newColor = CarpetColor.getColorByCarpet(itemInHand.getItem());
        if (newColor == null) {
            return false;
        }

        boolean hasCarpet = state.getValue(TableBlock.HAS_CARPET);
        CompoundTag nbt = getOrCreateNbt(info);

        if (!hasCarpet) {
            // 第一种情况：桌子上没有地毯，放置地毯
            if (!contraptionEntity.level().isClientSide()) {
                BlockState newState = state.setValue(TableBlock.HAS_CARPET, true);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(TABLE_CARPET_COLOR, newColor.getId());
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), newState, newNbt));
                itemInHand.shrink(1);
            }
            playSound(contraptionEntity, localPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // 第二种情况：有地毯，检查颜色是否不同
        int currentColorId = nbt.getInt(TABLE_CARPET_COLOR);
        DyeColor currentColor = DyeColor.byId(currentColorId);
        if (currentColor != newColor) {
            if (!contraptionEntity.level().isClientSide()) {
                // 掉落原地毯
                ItemStack carpetItem = CarpetColor.getCarpetByColor(currentColor).getDefaultInstance();
                BlockDrop.popResource(contraptionEntity.level(),
                        contraptionEntity.blockPosition(), 0.75, carpetItem);

                // 更新颜色
                BlockState newState = state.setValue(TableBlock.HAS_CARPET, true);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(TABLE_CARPET_COLOR, newColor.getId());
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), newState, newNbt));
                itemInHand.shrink(1);
            }
            playSound(contraptionEntity, localPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    /**
     * 从 NBT 读取物品列表到 ItemStackHandler
     */
    private ItemStackHandler readItems(CompoundTag nbt, RegistryAccess registryAccess) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        if (nbt.contains(TABLE_ITEMS)) {
            CompoundTag itemsTag = nbt.getCompound(TABLE_ITEMS);
            handler.deserializeNBT(registryAccess, itemsTag);
        }
        return handler;
    }

    /**
     * 将 ItemStackHandler 保存到 NBT
     */
    private void saveItems(CompoundTag nbt, ItemStackHandler handler, RegistryAccess registryAccess) {
        nbt.put(TABLE_ITEMS, handler.serializeNBT(registryAccess));
    }
}
