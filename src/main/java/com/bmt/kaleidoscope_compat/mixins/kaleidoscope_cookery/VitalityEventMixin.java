package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.event.effect.VitalityEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VitalityEvent.class)
public class VitalityEventMixin {

    @Inject(
            method = "onLivingDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            ),
            cancellable = true, remap = false
    )
    private static void kaleidoscopeCompat$checkBlacklist(LivingDeathEvent event, CallbackInfo ci) {
        if (!ForgeConfig.VITALITY_BLACKLIST.get().isEmpty()) {
            Entity entity = event.getEntity();
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (ForgeConfig.VITALITY_BLACKLIST.get().contains(entityId.toString())) {
                ci.cancel();
            }
        }
    }
}