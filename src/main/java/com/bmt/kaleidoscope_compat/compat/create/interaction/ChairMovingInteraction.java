package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.ChairBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.util.BlockDrop;
import com.github.ysbbbbbb.kaleidoscopecookery.util.CarpetColor;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.CHAIR_CARPET_COLOR;

/**
 * 椅子在动态结构上的交互行为
 */
public class ChairMovingInteraction extends BaseMovingInteraction {

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
        if (info == null || !(info.state().getBlock() instanceof ChairBlock)) {
            return false;
        }

        BlockState state = info.state();
        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 手持地毯 → 放置/替换地毯
        if (mainHandItem.is(ItemTags.WOOL_CARPETS)) {
            return handleCarpet(contraptionEntity, localPos, info, state, mainHandItem);
        }

        // 2. 空手 → 坐下
        int seatIndex = contraptionEntity.getContraption().getSeats().indexOf(localPos);
        if (seatIndex == -1) {
            return false;
        }

        if (contraptionEntity.level().isClientSide()) {
            return true;
        }

        contraptionEntity.addSittingPassenger(player, seatIndex);
        return true;
    }

    @Override
    public void handleEntityCollision(Entity entity, BlockPos localPos,
                                      AbstractContraptionEntity contraptionEntity) {
        int index = contraptionEntity.getContraption().getSeats().indexOf(localPos);
        if (index == -1)
            return;
        if (entity instanceof Player)
            return;
        contraptionEntity.addSittingPassenger(entity, index);
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

        boolean hasCarpet = state.getValue(ChairBlock.HAS_CARPET);
        CompoundTag nbt = getOrCreateNbt(info);

        if (!hasCarpet) {
            // 第一种情况：椅子上没有地毯，放置地毯
            if (!contraptionEntity.level().isClientSide()) {
                BlockState newState = state.setValue(ChairBlock.HAS_CARPET, true);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(CHAIR_CARPET_COLOR, newColor.getId());
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), newState, newNbt));
                itemInHand.shrink(1);
            }
            playSound(contraptionEntity, localPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // 第二种情况：有地毯，检查颜色是否不同
        int currentColorId = nbt.getInt(CHAIR_CARPET_COLOR);
        DyeColor currentColor = DyeColor.byId(currentColorId);
        if (currentColor != newColor) {
            if (!contraptionEntity.level().isClientSide()) {
                // 掉落原地毯
                ItemStack carpetItem = CarpetColor.getCarpetByColor(currentColor).getDefaultInstance();
                BlockDrop.popResource(contraptionEntity.level(),
                        BlockPos.containing(getGlobalPos(contraptionEntity, localPos)), 0.25, carpetItem);

                // 更新颜色和 BlockState
                BlockState newState = state.setValue(ChairBlock.HAS_CARPET, true);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(CHAIR_CARPET_COLOR, newColor.getId());
                updateData(contraptionEntity, localPos,
                        new StructureBlockInfo(info.pos(), newState, newNbt));
                itemInHand.shrink(1);
            }
            playSound(contraptionEntity, localPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }
}
