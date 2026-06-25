package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.CookStoolBlock;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * 烹饪凳在动态结构上的交互行为
 */
public class CookStoolMovingInteraction extends MovingInteractionBehaviour {

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
        if (info == null || !(info.state().getBlock() instanceof CookStoolBlock)) {
            return false;
        }

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
}
