package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.ENAMEL_BASIN_HAS_LID;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.ENAMEL_BASIN_OIL_COUNT;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock.HAS_LID;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock.OIL_COUNT;

/**
 * 搪瓷盆在动态结构上的交互行为
 * TODO:修复瓷盆交互模型渲染不同步问题
 */
public class EnamelBasinMovingInteraction extends BaseMovingInteraction {

    private static final int MAX_OIL_COUNT = 32;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof EnamelBasinBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        boolean hasLid = nbt.getBoolean(ENAMEL_BASIN_HAS_LID);
        int oilCount = nbt.getInt(ENAMEL_BASIN_OIL_COUNT);
        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 棍子敲击
        if (mainHandItem.is(Items.STICK)) {
            float pitch = 0.6F + contraptionEntity.level().random.nextFloat() * 0.2F;
            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 2.0F, pitch);
            return true;
        }

        // 2. 开盖
        if (hasLid) {
            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
            BlockState newState = info.state().setValue(HAS_LID, false);
            CompoundTag newNbt = nbt.copy();
            newNbt.putBoolean(ENAMEL_BASIN_HAS_LID, false);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            return true;
        }

        // 3. 空手盖盖
        if (mainHandItem.isEmpty()) {
            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.8F, 0.4F);
            BlockState newState = info.state().setValue(HAS_LID, true);
            CompoundTag newNbt = nbt.copy();
            newNbt.putBoolean(ENAMEL_BASIN_HAS_LID, true);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            return true;
        }

        // 4. 添加油脂
        if (mainHandItem.is(ModItems.OIL.get())) {
            if (oilCount >= MAX_OIL_COUNT) {
                return false;
            }
            int needCount = MAX_OIL_COUNT - oilCount;
            int consumeCount = Math.min(needCount, mainHandItem.getCount());
            int newOilCount = oilCount + consumeCount;

            if (!contraptionEntity.level().isClientSide()) {
                if (!player.isCreative()) {
                    mainHandItem.shrink(consumeCount);
                }
                // 更新BlockState的OIL_COUNT属性
                BlockState newState = info.state().setValue(OIL_COUNT, newOilCount);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(ENAMEL_BASIN_OIL_COUNT, newOilCount);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
            return true;
        }

        // 5/6. 铲子取油/还油
        if (mainHandItem.is(ModItems.KITCHEN_SHOVEL.get())) {
            return handleShovelInteraction(contraptionEntity, localPos, info, nbt, mainHandItem, oilCount);
        }

        return false;
    }

    /**
     * 铲子交互：取油或还油
     */
    private boolean handleShovelInteraction(AbstractContraptionEntity contraptionEntity,
                                            BlockPos localPos, StructureBlockInfo info, CompoundTag nbt,
                                            ItemStack shovel, int oilCount) {
        boolean shovelHasOil = KitchenShovelItem.hasOil(shovel);

        // 铲子有油，还油
        if (shovelHasOil) {
            if (oilCount >= MAX_OIL_COUNT) {
                return false;
            }

            int newOilCount = oilCount + 1;
            if (!contraptionEntity.level().isClientSide()) {
                KitchenShovelItem.setHasOil(shovel, false);
                BlockState newState = info.state().setValue(OIL_COUNT, newOilCount);
                CompoundTag newNbt = nbt.copy();
                newNbt.putInt(ENAMEL_BASIN_OIL_COUNT, newOilCount);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
            return true;
        }

        if (oilCount == 0) {
            return false;
        }

        // 取油
        int newOilCount = oilCount - 1;
        if (!contraptionEntity.level().isClientSide()) {
            KitchenShovelItem.setHasOil(shovel, true);
            BlockState newState = info.state().setValue(OIL_COUNT, newOilCount);
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(ENAMEL_BASIN_OIL_COUNT, newOilCount);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
        }

        playSound(contraptionEntity, localPos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 1.2F);
        return true;
    }
}
