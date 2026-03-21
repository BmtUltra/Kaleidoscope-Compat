package com.bmt.kaleidoscope_compat.compat.thirst;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThirstConfigGenerator {

    private static final Map<String, int[]> SOUP_ITEMS = new LinkedHashMap<>();

    static {
        SOUP_ITEMS.put("kaleidoscope_cookery:pork_bone_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_cookery:seafood_miso_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:fearsome_thick_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_cookery:lamb_and_radish_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:braised_beef_with_potatoes", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_cookery:wild_mushroom_rabbit_soup", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_cookery:tomato_beef_brisket_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:pufferfish_soup", new int[]{2, 4});
        SOUP_ITEMS.put("kaleidoscope_cookery:borscht", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_cookery:beef_meatball_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_cookery:chicken_and_mushroom_stew", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_cookery:donkey_soup", new int[]{6, 8});

        SOUP_ITEMS.put("kaleidoscope_nether:blaze_soup", new int[]{5, 7});
        SOUP_ITEMS.put("kaleidoscope_nether:wither_bone_soup", new int[]{4, 6});
        SOUP_ITEMS.put("kaleidoscope_nether:star_stew", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:soul_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:poisonous_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:magma_cream_soup", new int[]{3, 5});
        SOUP_ITEMS.put("kaleidoscope_nether:glowing_soup", new int[]{3, 5});

        SOUP_ITEMS.put("kaleidoscope_end:dragon_breath_chorus_soup", new int[]{6, 8});

        SOUP_ITEMS.put("kaleidoscope_chinesefood:seaweed_egg_drop_soup", new int[]{6, 8});
        SOUP_ITEMS.put("kaleidoscope_chinesefood:tomato_egg_drop_soup", new int[]{6, 8});
    }

    public static void generateConfig() {
        Path configDir = getConfigDirectory();
        Path thirstDir = configDir.resolve("thirst");
        Path thirstConfig = thirstDir.resolve("item_settings.toml");

        try {
            if (!Files.exists(thirstDir)) {
                Files.createDirectories(thirstDir);
            }

            if (Files.exists(thirstConfig)) {
                appendToExistingConfig(thirstConfig);
            } else {
                createNewConfig(thirstConfig);
            }

        } catch (IOException ignored) {
        }
    }

    private static Path getConfigDirectory() {
        return Path.of("config").toAbsolutePath();
    }

    private static void createNewConfig(Path configPath) throws IOException {
        StringBuilder config = new StringBuilder();

        config.append("[Drinks]\n");
        config.append("drinks = []\n\n");

        config.append("[Foods]\n");
        config.append("foods = [\n");

        for (Map.Entry<String, int[]> entry : SOUP_ITEMS.entrySet()) {
            int[] range = entry.getValue();
            config.append("    [\"")
                    .append(entry.getKey())
                    .append("\", ")
                    .append(range[0])
                    .append(", ")
                    .append(range[1])
                    .append("],\n");
        }

        config.append("]\n\n");

        config.append("[Blacklist]\n");
        config.append("itemsBlacklist = []\n");

        Files.writeString(configPath, config.toString(), StandardOpenOption.CREATE);
    }

    private static void appendToExistingConfig(Path configPath) throws IOException {
        String content = Files.readString(configPath);

        boolean needsUpdate = false;
        for (String item : SOUP_ITEMS.keySet()) {
            if (!content.contains(item)) {
                needsUpdate = true;
                break;
            }
        }

        if (!needsUpdate) {
            return;
        }

        int foodsIndex = content.indexOf("[Foods]");
        if (foodsIndex == -1) {
            StringBuilder appendText = new StringBuilder("\n\n[Foods]\nfoods = [\n");
            for (Map.Entry<String, int[]> entry : SOUP_ITEMS.entrySet()) {
                int[] range = entry.getValue();
                appendText.append("    [\"")
                        .append(entry.getKey())
                        .append("\", ")
                        .append(range[0])
                        .append(", ")
                        .append(range[1])
                        .append("],\n");
            }
            appendText.append("]\n");
            Files.writeString(configPath, content + appendText, StandardOpenOption.TRUNCATE_EXISTING);
            return;
        }

        int foodsArrayStart = content.indexOf("foods = [", foodsIndex);
        if (foodsArrayStart == -1) {
            StringBuilder newContent = getStringBuilder(content, foodsIndex);
            Files.writeString(configPath, newContent.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            return;
        }

        int bracketStart = content.indexOf("[", foodsArrayStart);
        int bracketEnd = findMatchingBracket(content, bracketStart);

        if (bracketEnd != -1) {
            StringBuilder insertText = new StringBuilder();
            if (bracketEnd > bracketStart + 1 && !isLastEntryComma(content, bracketEnd)) {
                insertText.append(",");
            }

            for (Map.Entry<String, int[]> entry : SOUP_ITEMS.entrySet()) {
                if (!content.contains(entry.getKey())) {
                    int[] range = entry.getValue();
                    insertText.append("    [\"")
                            .append(entry.getKey())
                            .append("\", ")
                            .append(range[0])
                            .append(", ")
                            .append(range[1])
                            .append("],\n");
                }
            }

            StringBuilder updatedContent = new StringBuilder(content);
            updatedContent.insert(bracketEnd, insertText);
            Files.writeString(configPath, updatedContent.toString(), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private static boolean isLastEntryComma(String content, int bracketEnd) {
        for (int i = bracketEnd - 1; i >= 0; i--) {
            char c = content.charAt(i);
            if (c == ',') return true;
            if (!Character.isWhitespace(c)) return false;
        }
        return false;
    }

    private static @NotNull StringBuilder getStringBuilder(String content, int foodsIndex) {
        int foodsSectionEnd = content.indexOf("\n\n", foodsIndex);
        if (foodsSectionEnd == -1) foodsSectionEnd = content.length();

        StringBuilder insertText = new StringBuilder("foods = [\n");
        for (Map.Entry<String, int[]> entry : SOUP_ITEMS.entrySet()) {
            int[] range = entry.getValue();
            insertText.append("    [\"")
                    .append(entry.getKey())
                    .append("\", ")
                    .append(range[0])
                    .append(", ")
                    .append(range[1])
                    .append("],\n");
        }
        insertText.append("]\n");

        StringBuilder newContent = new StringBuilder(content);
        newContent.insert(foodsSectionEnd, "\n" + insertText);
        return newContent;
    }

    private static int findMatchingBracket(String content, int start) {
        int bracketCount = 1;
        for (int i = start + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') bracketCount++;
            else if (c == ']') bracketCount--;

            if (bracketCount == 0) return i;
        }
        return -1;
    }
}