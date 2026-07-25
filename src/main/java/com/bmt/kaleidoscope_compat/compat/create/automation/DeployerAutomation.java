package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.bmt.kaleidoscope_compat.config.category.CreateCategory;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class DeployerAutomation {
    private DeployerAutomation() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!CreateCategory.createCompatEnabled || event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof DeployerFakePlayer)) {
            return;
        }

        BlockEntity target = event.getLevel().getBlockEntity(event.getPos());
        if (RecipeAutomation.configure(target, event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
