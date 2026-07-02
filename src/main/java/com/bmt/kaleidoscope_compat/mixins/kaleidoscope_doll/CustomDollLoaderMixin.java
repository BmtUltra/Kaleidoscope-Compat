package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll;

import com.bmt.kaleidoscope_compat.client.PlayerDollSkinManager;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import net.minecraft.client.model.Model;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(CustomDollLoader.class)
public abstract class CustomDollLoaderMixin {

    @Unique
    private static final String PLAYER_DOLL_PREFIX = "player_doll:";

    @Unique
    private static final String PLAYER_SKIN_MODEL = "geometry.player_skin";

    @Unique
    private static final String PLAYER_SKIN_MODEL_1 = "geometry.player_skin1";

    @Unique
    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetTexture(String name, CallbackInfoReturnable<ResourceLocation> cir) {
        if (name == null || !name.startsWith(PLAYER_DOLL_PREFIX)) return;

        String playerId = name.substring(PLAYER_DOLL_PREFIX.length());
        ResourceLocation skin = PlayerDollSkinManager.getSkin(playerId);
        cir.setReturnValue(Objects.requireNonNullElse(skin, DEFAULT_SKIN));
        cir.cancel();
    }

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetModel(String id, CallbackInfoReturnable<Model> cir) {
        if (id == null || !id.startsWith(PLAYER_DOLL_PREFIX)) return;

        String playerId = id.substring(PLAYER_DOLL_PREFIX.length());
        boolean isLegacy = PlayerDollSkinManager.isLegacySkin(playerId);
        String modelName = isLegacy ? PLAYER_SKIN_MODEL_1 : PLAYER_SKIN_MODEL;

        Model model = CustomDollLoader.getModel(modelName);
        if (model == null) {
            model = CustomDollLoader.getModel(isLegacy ? PLAYER_SKIN_MODEL : PLAYER_SKIN_MODEL_1);
        }
        if (model == null) {
            model = CustomDollLoader.getModel("geometry.zhiban");
        }
        if (model != null) {
            cir.setReturnValue(model);
            cir.cancel();
        }
    }

    @Inject(method = "getLanguage", at = @At("HEAD"), cancellable = true)
    private static void onGetLanguage(String locale, String key, CallbackInfoReturnable<String> cir) {
        if (key == null || !key.startsWith(PLAYER_DOLL_PREFIX)) return;

        String translatedName = Component.translatable("tooltip.kaleidoscope_compat.player_doll")
                .getString();
        cir.setReturnValue(translatedName);
        cir.cancel();
    }
}
