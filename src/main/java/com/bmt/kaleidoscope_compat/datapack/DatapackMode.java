package com.bmt.kaleidoscope_compat.datapack;

import net.minecraft.network.chat.Component;

public enum DatapackMode {
    NONE,
    COMPAT,
    UNITE;

    public Component getDisplayName() {
        return Component.translatable("enum.kaleidoscope_compat.datapack_mode." + this.name().toLowerCase());
    }
}