package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll;

import com.bmt.kaleidoscope_compat.client.PlayerDollSkinManager;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import net.minecraft.client.model.Model;
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

        Model model = CustomDollLoader.getModel(PLAYER_SKIN_MODEL);
        if (model == null) {
            model = CustomDollLoader.getModel("geometry.zhiban");
        }
        if (model != null) {
            cir.setReturnValue(model);
            cir.cancel();
        }
    }
}