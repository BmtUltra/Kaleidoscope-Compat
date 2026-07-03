package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll;

import com.github.ysbbbbbb.kaleidoscopedoll.inventory.ComputerMenu;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ComputerMenu.class)
public interface ComputerMenuAccessor {
    @Accessor
    ItemStackHandler getInput();

    @Accessor
    ItemStackHandler getOutput();
}