package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MillstoneBlockEntity.class)
public interface MillstoneBlockEntityAccessor {
    @Accessor("input")
    ItemStack getInput();

    @Accessor("input")
    void setInput(ItemStack input);

    @Accessor("progress")
    int getProgress();

    @Accessor("progress")
    void setProgress(int progress);

    @Accessor("rotSpeedTick")
    float getRotSpeedTick();
}