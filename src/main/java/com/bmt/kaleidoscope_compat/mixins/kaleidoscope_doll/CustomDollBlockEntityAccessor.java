package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll;

import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.CustomDollBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomDollBlockEntity.class)
public interface CustomDollBlockEntityAccessor {
    @Accessor("modelId")
    String getModelId();
}