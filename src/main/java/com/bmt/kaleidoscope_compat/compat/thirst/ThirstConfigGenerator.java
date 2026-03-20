package com.bmt.kaleidoscope_compat.compat.thirst;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ThirstConfigGenerator {

    private static final String[] SOUP_ITEMS = {
            "kaleidoscope_cookery:pork_bone_soup",
            "kaleidoscope_cookery:seafood_miso_soup",
            "kaleidoscope_cookery:fearsome_thick_soup",
            "kaleidoscope_cookery:lamb_and_radish_soup",
            "kaleidoscope_cookery:braised_beef_with_potatoes",
            "kaleidoscope_cookery:wild_mushroom_rabbit_soup",
            "kaleidoscope_cookery:tomato_beef_brisket_soup",
            "kaleidoscope_cookery:pufferfish_soup",
            "kaleidoscope_cookery:borscht",
            "kaleidoscope_cookery:beef_meatball_soup",
            "kaleidoscope_cookery:chicken_and_mushroom_stew"
    };

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

        } catch (IOException ignored){

        }

    }

    private static Path getConfigDirectory() {
        return Path.of("config").toAbsolutePath();
    }

    private static void createNewConfig(Path configPath) throws IOException {
        StringBuilder config = new StringBuilder();

        config.append("# Thirst Was Taken 配置 - Kaleidoscope Cookery 兼容\n");
        config.append("# 自动生成于: ").append(java.time.LocalDateTime.now()).append("\n\n");

        config.append("[Drinks]\n");
        config.append("# 饮用时恢复口渴的物品\n");
        config.append("drinks = []\n\n");

        config.append("[Foods]\n");
        config.append("# 食用时恢复口渴的物品\n");
        config.append("foods = [\n");

        for (int i = 0; i < SOUP_ITEMS.length; i++) {
            config.append("    [\"").append(SOUP_ITEMS[i]).append("\", 4, 6]");
            if (i < SOUP_ITEMS.length - 1) {
                config.append(",");
            }
            config.append("\n");
        }

        config.append("]\n\n");

        config.append("[Blacklist]\n");
        config.append("# 禁用口渴支持的物品\n");
        config.append("itemsBlacklist = []\n");

        Files.writeString(configPath, config.toString(), StandardOpenOption.CREATE);
    }

    private static void appendToExistingConfig(Path configPath) throws IOException {
        String content = Files.readString(configPath);

        boolean needsUpdate = true;
        for (String item : SOUP_ITEMS) {
            if (content.contains(item)) {
                needsUpdate = false;
                break;
            }
        }

        if (needsUpdate) {
            int foodsIndex = content.indexOf("[Foods]");
            if (foodsIndex == -1) {
                StringBuilder appendText = new StringBuilder("\n\n[Foods]\nfoods = [\n");
                for (String item : SOUP_ITEMS) {
                    appendText.append("    [\"").append(item).append("\", 4, 6],\n");
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
                if (bracketEnd > bracketStart + 1) {
                    insertText.append(",");
                }
                insertText.append("\n    # Kaleidoscope Cookery 汤类食物\n");
                for (String item : SOUP_ITEMS) {
                    insertText.append("    [\"").append(item).append("\", 4, 6],\n");
                }

                StringBuilder updatedContent = new StringBuilder(content);
                updatedContent.insert(bracketEnd, insertText);

                Files.writeString(configPath, updatedContent.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            }
        } else {
        }
    }

    private static @NotNull StringBuilder getStringBuilder(String content, int foodsIndex) {
        int foodsSectionEnd = content.indexOf("\n\n", foodsIndex);
        if (foodsSectionEnd == -1) foodsSectionEnd = content.length();

        StringBuilder insertText = new StringBuilder("foods = [\n");
        for (String item : SOUP_ITEMS) {
            insertText.append("    [\"").append(item).append("\", 4, 6],\n");
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