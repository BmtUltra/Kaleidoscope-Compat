package com.bmt.kaleidoscope_compat.datapack;

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DatapackMode implements StringRepresentable, Translatable {
    NONE("none", "datapack_mode.kaleidoscope_compat.none"),
    COMPAT("compat", "datapack_mode.kaleidoscope_compat.compat"),
    UNITE("unite", "datapack_mode.kaleidoscope_compat.unite");

    private final String name;
    private final String translationKey;

    DatapackMode(String name, String translationKey) {
        this.name = name;
        this.translationKey = translationKey;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}