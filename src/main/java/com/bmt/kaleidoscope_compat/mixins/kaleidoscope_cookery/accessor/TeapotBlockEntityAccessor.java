package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TeapotBlockEntity.class)
public interface TeapotBlockEntityAccessor {
    @Accessor("input")
    @Mutable
    void kaleidoscopeCompat$setInput(ItemStack input);

    @Accessor("teaFluidId")
    @Mutable
    void kaleidoscopeCompat$setTeaFluidId(ResourceLocation teaFluidId);

    @Accessor("result")
    @Mutable
    void kaleidoscopeCompat$setResult(ItemStack result);

    @Accessor("status")
    @Mutable
    void kaleidoscopeCompat$setStatus(int status);

    @Accessor("currentTick")
    @Mutable
    void kaleidoscopeCompat$setCurrentTick(int currentTick);
}
