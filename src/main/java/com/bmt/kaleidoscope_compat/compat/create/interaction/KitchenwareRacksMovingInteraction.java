package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.client.ClientRayTraceHelper;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.bmt.kaleidoscope_compat.network.ContraptionRacksInteractPayload;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.KitchenwareRacksBlock;
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
import net.neoforged.neoforge.common.Tags;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.KITCHENWARE_RACKS_LEFT_ITEM;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.KITCHENWARE_RACKS_RIGHT_ITEM;

/**
 * 厨具架在动态结构上的交互行为
 */
public class KitchenwareRacksMovingInteraction extends BaseMovingInteraction {

    /**
     * 服务端缓存：玩家上次射线检测的左右侧结果。
     */
    private static final Map<UUID, Boolean> PENDING_SIDE = new ConcurrentHashMap<>();

    /**
     * 供网络包处理器调用：缓存玩家的左右侧判断结果
     */
    public static void cacheSide(Player player, boolean isLeft) {
        PENDING_SIDE.put(player.getUUID(), isLeft);
    }

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof KitchenwareRacksBlock)) {
            return false;
        }

        if (contraptionEntity.level().isClientSide()) {
            boolean isLeft = determineSideClient(info, contraptionEntity, player);
            ContraptionRacksInteractPayload.sendToServer(contraptionEntity.getId(), localPos, isLeft);
            return true;
        }

        Boolean cachedSide = PENDING_SIDE.remove(player.getUUID());
        boolean isLeft = cachedSide != null ? cachedSide : true;

        CompoundTag nbt = getOrCreateNbt(info);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

        ItemStack itemLeft = readItem(nbt, KITCHENWARE_RACKS_LEFT_ITEM, registryAccess);
        ItemStack itemRight = readItem(nbt, KITCHENWARE_RACKS_RIGHT_ITEM, registryAccess);
        ItemStack stackInRacks = isLeft ? itemLeft : itemRight;

        ItemStack mainHandItem = player.getMainHandItem();

        // 取出物品
        if (mainHandItem.isEmpty() && !stackInRacks.isEmpty()) {
            ContraptionUtil.giveItemToPlayer(player, stackInRacks.copy());

            CompoundTag newNbt = nbt.copy();
            if (isLeft) {
                newNbt.put(KITCHENWARE_RACKS_LEFT_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
            } else {
                newNbt.put(KITCHENWARE_RACKS_RIGHT_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
            }
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));

            playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // 放入工具
        if (mainHandItem.is(Tags.Items.TOOLS) && stackInRacks.isEmpty()) {
            ItemStack toInsert = mainHandItem.split(1);

            CompoundTag newNbt = nbt.copy();
            if (isLeft) {
                newNbt.put(KITCHENWARE_RACKS_LEFT_ITEM, toInsert.saveOptional(registryAccess));
            } else {
                newNbt.put(KITCHENWARE_RACKS_RIGHT_ITEM, toInsert.saveOptional(registryAccess));
            }
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));

            playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    /**
     * 客户端：通过射线检测判断左右侧
     */
    private boolean determineSideClient(StructureBlockInfo info,
                                         AbstractContraptionEntity contraptionEntity,
                                         Player player) {
        return ClientRayTraceHelper
                .determineRacksSideFromRayTrace(info, contraptionEntity, player);
    }

    private ItemStack readItem(CompoundTag nbt, String key, RegistryAccess registryAccess) {
        if (nbt.contains(key)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(key));
        }
        return ItemStack.EMPTY;
    }
}
