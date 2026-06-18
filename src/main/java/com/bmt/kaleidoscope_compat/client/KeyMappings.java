package com.bmt.kaleidoscope_compat.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定注册
 */
public class KeyMappings {

    public static final KeyMapping TAKE_KITCHEN_ITEM = new KeyMapping(
            "key.kaleidoscope_compat.take_kitchen_item",
            GLFW.GLFW_KEY_V,
            "key.category.kaleidoscope_compat"
    );

    public static void init() {
    }
}
