package com.bmt.kaleidoscope_compat.mixins.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin {

    @Inject(method = "canBindEntity", at = @At("HEAD"), cancellable = true)
    private void onCanBindEntity(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof EntityMaid maid) {
            maid.getTask();
            if ("millstone".equals(maid.getTask().getUid().getPath())) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}