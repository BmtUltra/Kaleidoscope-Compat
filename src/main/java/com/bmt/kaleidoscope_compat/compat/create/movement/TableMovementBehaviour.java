package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.TableBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 桌子在动态结构上的移动行为
 */
public class TableMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof TableBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return false;
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
    }
}
