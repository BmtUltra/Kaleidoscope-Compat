package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotBlockEntity.class)
public interface PotBlockEntityAccessor {
    @Accessor("carrier")
    Ingredient kaleidoscopeCompat$getCarrier();

    @Accessor("currentTick")
    @Mutable
    void kaleidoscopeCompat$setCurrentTick(int currentTick);

    @Accessor("status")
    @Mutable
    void kaleidoscopeCompat$setStatus(int status);

    @Accessor("stirFryCount")
    int kaleidoscopeCompat$getStirFryCount();

    @Accessor("stirFryCount")
    @Mutable
    void kaleidoscopeCompat$setStirFryCount(int stirFryCount);

    @Accessor("seed")
    @Mutable
    void kaleidoscopeCompat$setSeed(long seed);

    @Invoker("startCooking")
    void kaleidoscopeCompat$invokeStartCooking(Level level);
}
