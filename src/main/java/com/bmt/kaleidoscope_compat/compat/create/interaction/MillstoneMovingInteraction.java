package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * 石磨在动态结构上的交互行为
 * TODO: 实现石磨在动态结构上的交互
 */
public class MillstoneMovingInteraction extends BaseMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        // TODO: 实现交互逻辑
        return false;
    }
}
