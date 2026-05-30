package com.bmt.kaleidoscope_compat.mixins.quark;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SickleItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.api.event.SimpleHarvestEvent;

@Mixin(SimpleHarvestEvent.class)
public class SimpleHarvestEventMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void kc$checkSickleOnHarvest(
            net.minecraft.world.level.block.state.BlockState blockState,
            net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.Level level,
            InteractionHand hand,
            Entity entity,
            SimpleHarvestEvent.ActionType originalActionType,
            CallbackInfo ci
    ) {
        if (!MainConfig.quarkSickleHarvestFixEnabledValue) return;

        if (entity instanceof Player player && hand != null) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() instanceof SickleItem) {
                ((SimpleHarvestEvent)(Object)this).setCanceled(true);
            }
        }
    }
}