package com.bmt.kaleidoscope_compat.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerSkinFetcher {
    private static final String MOJANG_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final int TIMEOUT = 5000;

    public record PlayerSkinInfo(UUID uuid, String skinUrl, boolean slimModel, boolean legacySkin) {}

    public static CompletableFuture<Optional<UUID>> fetchPlayerUuid(String username) {
        return CompletableFuture.supplyAsync(() -> fetchUuidSync(username));
    }

    public static CompletableFuture<Optional<PlayerSkinInfo>> fetchPlayerSkinInfo(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var uuidOpt = fetchUuidSync(username);
                if (uuidOpt.isEmpty()) return Optional.empty();

                UUID uuid = uuidOpt.get();
                var profileOpt = fetchProfile(uuid);
                if (profileOpt.isEmpty()) return Optional.empty();

                return extractSkinInfo(uuid, profileOpt.get());
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    public static CompletableFuture<Optional<byte[]>> downloadSkin(String skinUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection conn = createConnection(skinUrl);
                if (conn.getResponseCode() != 200) return Optional.empty();
                try (var is = conn.getInputStream()) {
                    return Optional.of(is.readAllBytes());
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    private static Optional<UUID> fetchUuidSync(String username) {
        try {
            HttpURLConnection conn = createConnection(MOJANG_API + username);
            int code = conn.getResponseCode();
            if (code == 204 || code == 404) return Optional.empty();
            if (code != 200) return Optional.empty();

            try (var reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                return Optional.of(parseUuid(json.get("id").getAsString()));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<JsonObject> fetchProfile(UUID uuid) {
        try {
            String uuidNoDashes = uuid.toString().replace("-", "");
            HttpURLConnection conn = createConnection(SESSION_API + uuidNoDashes);
            if (conn.getResponseCode() != 200) return Optional.empty();

            try (var reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                return Optional.of(JsonParser.parseReader(reader).getAsJsonObject());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<PlayerSkinInfo> extractSkinInfo(UUID uuid, JsonObject profile) {
        var properties = profile.getAsJsonArray("properties");
        if (properties == null || properties.isEmpty()) return Optional.empty();

        for (var prop : properties) {
            JsonObject propObj = prop.getAsJsonObject();
            if (!"textures".equals(propObj.get("name").getAsString())) continue;

            String decoded = new String(Base64.getDecoder().decode(
                    propObj.get("value").getAsString()), StandardCharsets.UTF_8);
            JsonObject textures = JsonParser.parseString(decoded)
                    .getAsJsonObject().getAsJsonObject("textures");
            if (textures == null) continue;

            JsonObject skin = textures.getAsJsonObject("SKIN");
            if (skin == null) continue;

            String skinUrl = skin.get("url").getAsString();
            boolean slimModel = skin.has("metadata") &&
                    "slim".equals(skin.getAsJsonObject("metadata").get("model").getAsString());
            return Optional.of(new PlayerSkinInfo(uuid, skinUrl, slimModel, false));
        }
        return Optional.empty();
    }

    private static HttpURLConnection createConnection(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private static UUID parseUuid(String idWithoutDashes) {
        if (idWithoutDashes.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID format: " + idWithoutDashes);
        }
        String formatted = idWithoutDashes.substring(0, 8) + "-" +
                idWithoutDashes.substring(8, 12) + "-" +
                idWithoutDashes.substring(12, 16) + "-" +
                idWithoutDashes.substring(16, 20) + "-" +
                idWithoutDashes.substring(20);
        return UUID.fromString(formatted);
    }
}