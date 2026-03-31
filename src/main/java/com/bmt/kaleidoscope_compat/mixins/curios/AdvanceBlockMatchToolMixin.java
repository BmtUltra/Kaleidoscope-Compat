package com.bmt.kaleidoscope_compat.mixins.curios;

import com.bmt.kaleidoscope_compat.compat.curios.CuriosIntegration;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvanceBlockMatchTool.class,remap = false)
public class AdvanceBlockMatchToolMixin {

    @Shadow
    @Final
    private EquipmentSlot slot;

    @Inject(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void checkCuriosHeadSlot(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        if (this.slot != EquipmentSlot.HEAD) {
            return;
        }
        if (!CuriosIntegration.isLoaded()) {
            return;
        }

        if (context.hasParam(LootContextParams.THIS_ENTITY)) {
            Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
            if (entity instanceof LivingEntity livingEntity) {
                if (CuriosIntegration.hasStrawHatInCurios(livingEntity)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}