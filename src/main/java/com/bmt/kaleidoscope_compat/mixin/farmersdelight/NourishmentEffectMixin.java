package com.bmt.kaleidoscope_compat.mixin.farmersdelight;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.effect.NourishmentEffect;

@Mixin(NourishmentEffect.class)
public abstract class NourishmentEffectMixin extends MobEffect {
    protected NourishmentEffectMixin(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Inject(
        method = "applyEffectTick",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onApplyEffectTick(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (!entity.getCommandSenderWorld().isClientSide && entity instanceof Player player) {
            if (player.hasEffect(ModEffects.SATIATED_SHIELD)) {
                cir.setReturnValue(true);
            }
        }
    }
}