package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.minecraft.tags.TagLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagLoader.class)
public class TagLoaderMixin {

    @Redirect(
            method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private void onTagLoadError(Logger logger, String message, Object[] args) {
        if (!MainConfig.suppressTagLoadErrors) {
            logger.error(message, args);
        }
    }
}