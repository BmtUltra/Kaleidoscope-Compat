package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin {

    @Inject(method = "canBindEntity", at = @At("HEAD"), cancellable = true,remap = false)
    private void onCanBindEntity(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        String className = mob.getClass().getName();
        if ("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid".equals(className)) {
            try {
                Object task = mob.getClass().getMethod("getTask").invoke(mob);
                String uid = task.getClass().getMethod("getUid").invoke(task).toString();
                if (uid.contains("millstone")) {
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            } catch (Exception ignored) {
            }
        }
    }
}