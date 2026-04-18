package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_end;

import com.bmt.kaleidoscope_end.block.KECropBlockBase;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SickleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KECropBlockBase.class)
public abstract class CropBlockBaseMixin {

    @Inject(
        method = "useWithoutItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kc$preventSickleHarvest(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        if (mainHandItem.getItem() instanceof SickleItem || offHandItem.getItem() instanceof SickleItem) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}