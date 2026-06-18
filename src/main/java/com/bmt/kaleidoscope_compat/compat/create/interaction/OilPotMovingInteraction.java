package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.OIL_POT_OIL_COUNT;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock.HAS_OIL;

/**
 * 油壶在动态结构上的交互行为
 */
public class OilPotMovingInteraction extends BaseMovingInteraction {

    private static final int MAX_OIL_COUNT = 256;
    private static final int TAKE_OIL_MAX = 64;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof OilPotBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        int oilCount = nbt.getInt(OIL_POT_OIL_COUNT);
        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 空手取出油
        if (mainHandItem.isEmpty()) {
            if (oilCount <= 0) {
                return false;
            }
            int takeCount = Math.min(oilCount, TAKE_OIL_MAX);
            ItemStack oilStack = new ItemStack(ModItems.OIL.get(), takeCount);
            int newOilCount = oilCount - takeCount;

            if (!contraptionEntity.level().isClientSide()) {
                ContraptionUtil.giveItemToPlayer(player, oilStack);

                BlockState newState = info.state().setValue(HAS_OIL, newOilCount > 0);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(OIL_POT_OIL_COUNT, newOilCount);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 1.0F,
                    0.8F + contraptionEntity.level().random.nextFloat() * 0.2F);
            return true;
        }

        // 2. 手持油脂添加
        if (mainHandItem.is(ModItems.OIL.get())) {
            int needCount = MAX_OIL_COUNT - oilCount;
            if (needCount <= 0) {
                return false;
            }
            int addCount = Math.min(needCount, mainHandItem.getCount());
            int newOilCount = oilCount + addCount;

            if (!contraptionEntity.level().isClientSide()) {
                if (!player.isCreative()) {
                    mainHandItem.shrink(addCount);
                }

                BlockState newState = info.state().setValue(HAS_OIL, newOilCount > 0);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(OIL_POT_OIL_COUNT, newOilCount);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 1.0F,
                    0.4F + contraptionEntity.level().random.nextFloat() * 0.2F);
            return true;
        }

        return false;
    }
}
