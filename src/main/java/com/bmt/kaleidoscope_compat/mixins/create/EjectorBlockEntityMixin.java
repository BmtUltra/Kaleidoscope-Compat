package com.bmt.kaleidoscope_compat.mixins.create;

import com.bmt.kaleidoscope_compat.compat.create.automation.EjectorAutomation;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EjectorBlockEntity.class)
public class EjectorBlockEntityMixin {
    @Shadow
    @Nullable
    Pair<Vec3, BlockPos> earlyTarget;

    @Inject(
            method = "placeItemAtTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/depot/EjectorBlockEntity;getTargetOpenInv()Lcom/simibubi/create/content/kinetics/belt/behaviour/DirectBeltInputBehaviour;"
            )
    )
    // Need an Ejector compatibility API from Create
    private void kaleidoscopeCompat$insertIntoWorkBlock(boolean doLogic, float maxTime,
                                                        IntAttached<ItemStack> intAttached, CallbackInfo ci) {
        EjectorBlockEntity ejector = (EjectorBlockEntity) (Object) this;
        if (ejector.getLevel() == null) {
            return;
        }
        BlockPos targetPos = earlyTarget == null ? ejector.getTargetPosition() : earlyTarget.getSecond();
        ItemStack remainder = EjectorAutomation.insertAtTarget(ejector.getLevel(), targetPos, intAttached.getSecond());
        intAttached.setSecond(remainder);
    }
}
