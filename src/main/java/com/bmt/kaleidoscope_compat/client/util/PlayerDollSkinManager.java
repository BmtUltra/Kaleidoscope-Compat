package com.bmt.kaleidoscope_compat.client.util;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.util.PlayerSkinFetcher;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class PlayerDollSkinManager {
    private static final Map<String, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LEGACY_SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();

    public static ResourceLocation getSkin(String playerId) {
        String lowerId = playerId.toLowerCase();

        ResourceLocation cached = SKIN_CACHE.get(lowerId);
        if (cached != null) {
            return cached;
        }

        if (LOADING.contains(lowerId)) {
            return null;
        }

        loadSkinAsync(lowerId);
        return null;
    }

    public static boolean isLegacySkin(String playerId) {
        String lowerId = playerId.toLowerCase();
        return LEGACY_SKIN_CACHE.getOrDefault(lowerId, false);
    }

    private static void loadSkinAsync(String playerId) {
        LOADING.add(playerId);

        CompletableFuture.runAsync(() -> {
            try {
                var skinInfoOpt = PlayerSkinFetcher.fetchPlayerSkinInfo(playerId).join();
                if (skinInfoOpt.isEmpty() || skinInfoOpt.get().skinUrl() == null) {
                    return;
                }

                var skinDataOpt = PlayerSkinFetcher.downloadSkin(skinInfoOpt.get().skinUrl()).join();
                if (skinDataOpt.isEmpty()) {
                    return;
                }

                byte[] skinData = skinDataOpt.get();
                Minecraft.getInstance().execute(() -> registerTexture(playerId, skinData));
            } catch (Exception ignored) {
            } finally {
                LOADING.remove(playerId);
            }
        });
    }

    private static void registerTexture(String playerId, byte[] skinData) {
        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(skinData));
            ResourceLocation textureId = KaleidoscopeCompat.id("player_skin/" + playerId);
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(textureId, texture);
            SKIN_CACHE.put(playerId, textureId);
            boolean isLegacy = image.getHeight() == 32 && image.getWidth() == 64;
            LEGACY_SKIN_CACHE.put(playerId, isLegacy);
        } catch (Exception ignored) {
        }
    }
}