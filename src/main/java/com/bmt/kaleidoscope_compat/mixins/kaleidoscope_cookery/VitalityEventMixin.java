package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.event.effect.VitalityEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VitalityEvent.class, remap = false)
public class VitalityEventMixin {

    @Inject(
            method = "onLivingDeath",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void kaleidoscopeCompat$checkBlacklist(LivingDeathEvent event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (entity != null && entity.getType().is(TagUtil.EntityTypes.VITALITY_BLACKLIST)) {
            ci.cancel();
        }
    }
}