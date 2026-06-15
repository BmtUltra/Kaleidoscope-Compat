package com.bmt.kaleidoscope_compat.compat.create;

/**
 * 入口类：注册森罗厨房方块在 Create 动态结构上的行为
 */
public class CreatePotMovingInteraction {
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }

        CreateKitchenBehaviours.register();
        initialized = true;
    }
}
