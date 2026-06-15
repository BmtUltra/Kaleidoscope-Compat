package com.bmt.kaleidoscope_compat.compat.create.util;

/**
 * 统一管理所有厨房方块在动态结构上使用的 NBT 键名和状态常量
 */
public final class ContraptionNbtKeys {

    private ContraptionNbtKeys() {}

    // ====== 通用 NBT 键 ======
    public static final String STATUS = "Status";
    public static final String CURRENT_TICK = "CurrentTick";
    public static final String SEED = "Seed";
    public static final String INPUTS = "Inputs";
    public static final String RESULT = "Result";
    public static final String CARRIER = "Carrier";

    // ====== Pot 炒锅专用 NBT 键 ======
    public static final String POT_STIR_FRY_COUNT = "StirFryCount";

    // ====== Stockpot 汤锅专用 NBT 键 ======
    public static final String STOCKPOT_RECIPE_ID = "RecipeId";
    public static final String STOCKPOT_SOUP_BASE_ID = "SoupBaseId";
    public static final String STOCKPOT_TAKEOUT_COUNT = "TakeoutCount";
    public static final String STOCKPOT_LID_ITEM = "LidItem";
    public static final String STOCKPOT_CARRIER = "StockpotCarrier";

    // ====== Steamer 蒸笼专用 NBT 键 ======
    public static final String STEAMER_ITEMS = "Items";
    public static final String STEAMER_COOKING_PROGRESS = "CookingProgress";
    public static final String STEAMER_COOKING_TIME = "CookingTime";
    public static final String STEAMER_LIT_LEVEL = "LitLevel";

    // ====== Pot 炒锅状态常量 ======
    public static final class PotStatus {
        public static final int PUT_INGREDIENT = 0;
        public static final int COOKING = 1;
        public static final int FINISHED = 2;
        public static final int BURNT = 3;

        private PotStatus() {}
    }

    // ====== Stockpot 汤锅状态常量 ======
    public static final class StockpotStatus {
        public static final int PUT_SOUP_BASE = 0;
        public static final int PUT_INGREDIENT = 1;
        public static final int COOKING = 2;
        public static final int FINISHED = 3;

        private StockpotStatus() {}
    }
}
