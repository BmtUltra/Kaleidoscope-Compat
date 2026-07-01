package com.bmt.kaleidoscope_compat.mixins.create.accessor;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ArmBlockEntity.class)
public interface ArmBlockEntityAccessor {
    
    @Accessor
    List<ArmInteractionPoint> getInputs();
    
    @Accessor
    List<ArmInteractionPoint> getOutputs();
}