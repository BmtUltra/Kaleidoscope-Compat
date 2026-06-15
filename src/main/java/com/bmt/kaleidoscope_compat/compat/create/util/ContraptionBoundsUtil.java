package com.bmt.kaleidoscope_compat.compat.create.util;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Contraption 边界计算工具类
 * 用于在动态添加/移除方块时重新计算 Contraption 的 bounds
 */
public class ContraptionBoundsUtil {

    /**
     * 重新计算 Contraption 的 bounds
     */
    public static AABB recalculateBounds(Contraption contraption) {
        calculateMinMaxBounds(contraption);
        AABB newBounds;

        if (contraption instanceof com.simibubi.create.content.contraptions.bearing.BearingContraption) {
            contraption.expandBoundsAroundAxis(Direction.Axis.Y);
        } else if (contraption instanceof com.simibubi.create.content.contraptions.mounted.MountedContraption) {
            contraption.expandBoundsAroundAxis(Direction.Axis.Y);
        } else {
            contraption.expandBoundsAroundAxis(Direction.Axis.Y);
        }

        newBounds = contraption.bounds;
        return newBounds;
    }

    /**
     * 计算所有方块的 minmax bounds
     */
    private static void calculateMinMaxBounds(Contraption contraption) {
        AABB newBounds = new AABB(BlockPos.ZERO);
        for (BlockPos pos : contraption.getBlocks().keySet()) {
            newBounds = newBounds.minmax(new AABB(pos));
        }
    }
}
