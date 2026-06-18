package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.ENAMEL_BASIN_HAS_LID;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.ENAMEL_BASIN_OIL_COUNT;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock.HAS_LID;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.EnamelBasinBlock.OIL_COUNT;

/**
 * 搪瓷盆在动态结构上的移动行为
 */
public class EnamelBasinMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof EnamelBasinBlock;
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
        if (info == null || !(info.state().getBlock() instanceof EnamelBasinBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        int oilCount = nbt.getInt(ENAMEL_BASIN_OIL_COUNT);

        // 掉落油脂
        if (oilCount > 0) {
            List<ItemStack> drops = new ArrayList<>();
            drops.add(new ItemStack(ModItems.OIL.get(), oilCount));

            // 油脂通过方块掉落系统处理
            // 这里只重置NBT状态
        }

        // 重置状态并同步BlockState
        BlockState newState = info.state()
                .setValue(HAS_LID, true)
                .setValue(OIL_COUNT, 0);
        CompoundTag newNbt = nbt.copy();
        newNbt.putBoolean(ENAMEL_BASIN_HAS_LID, true);
        newNbt.putInt(ENAMEL_BASIN_OIL_COUNT, 0);
        updateData(context, newState, newNbt);
    }
}
