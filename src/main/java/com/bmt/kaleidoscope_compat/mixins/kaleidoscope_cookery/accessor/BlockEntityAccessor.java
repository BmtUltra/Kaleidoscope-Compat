package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {
    @Accessor("level")
    Level getLevel();

    @Accessor("worldPosition")
    BlockPos getWorldPosition();

    @Invoker("setChanged")
    void invokeSetChanged();
}