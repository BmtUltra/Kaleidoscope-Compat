package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.kitchen.effect.ProjectileDodgeConfig;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.MobEffectInstanceAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.event.effect.ProjectileDodgeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileDodgeEvent.class)
public class ProjectileDodgeEventMixin {

    @Inject(
            method = "onProjectileHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopecookery/event/effect/ProjectileDodgeEvent;randomTeleport(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;DI)V"
            ),
            cancellable = true
    )
    private static void kaleidoscopeCompat$disableTeleport(ProjectileImpactEvent event, CallbackInfo ci) {
        if (!ProjectileDodgeConfig.teleportEnabled) {
            HitResult hit = event.getRayTraceResult();
            if (hit instanceof EntityHitResult hitResult
                    && hitResult.getEntity() instanceof LivingEntity living
                    && living.hasEffect(ModEffects.PROJECTILE_DODGE)
            ) {
                MobEffectInstance instance = living.getEffect(ModEffects.PROJECTILE_DODGE);
                if (instance != null && !instance.isInfiniteDuration()) {
                    MobEffectInstanceAccessor accessor = (MobEffectInstanceAccessor) instance;
                    int newDuration = accessor.getDuration() - ProjectileDodgeConfig.durationCost;
                    if (newDuration <= 0) {
                        living.removeEffect(ModEffects.PROJECTILE_DODGE);
                    } else {
                        accessor.setDuration(newDuration);
                        living.forceAddEffect(instance, null);
                    }
                }
            }
            ci.cancel();
        }
    }
}