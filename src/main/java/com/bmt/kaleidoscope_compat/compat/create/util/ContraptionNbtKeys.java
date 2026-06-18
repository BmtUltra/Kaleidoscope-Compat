package com.bmt.kaleidoscope_compat.compat.create.util;

/**
 * 统一管理所有厨房方块在动态结构上使用的 NBT 键名和状态常量
 */
public final class ContraptionNbtKeys {

    private ContraptionNbtKeys() {}

    public static final String STATUS = "Status";
    public static final String CURRENT_TICK = "CurrentTick";
    public static final String SEED = "Seed";
    public static final String INPUTS = "Inputs";
    public static final String RESULT = "Result";
    public static final String CARRIER = "Carrier";

    public static final String POT_STIR_FRY_COUNT = "StirFryCount";

    public static final String STOCKPOT_RECIPE_ID = "RecipeId";
    public static final String STOCKPOT_SOUP_BASE_ID = "SoupBaseId";
    public static final String STOCKPOT_TAKEOUT_COUNT = "TakeoutCount";
    public static final String STOCKPOT_LID_ITEM = "LidItem";
    public static final String STOCKPOT_CARRIER = "StockpotCarrier";

    public static final String STEAMER_COOKING_PROGRESS = "CookingProgress";
    public static final String STEAMER_COOKING_TIME = "CookingTime";
    public static final String STEAMER_LIT_LEVEL = "LitLevel";

    public static final String TEA_FLUID_ID = "TeaFluidId";
    public static final String TEAPOT_INPUT = "Input";

    public static final String CHOPPING_BOARD_CURRENT_CUT_STACK = "CurrentCutStack";
    public static final String CHOPPING_BOARD_RESULT_ITEM = "ResultItem";
    public static final String CHOPPING_BOARD_MAX_CUT_COUNT = "MaxCutCount";
    public static final String CHOPPING_BOARD_CURRENT_CUT_COUNT = "CurrentCutCount";
    public static final String CHOPPING_BOARD_MODEL_ID = "ModelId";

    public static final String ENAMEL_BASIN_HAS_LID = "HasLid";
    public static final String ENAMEL_BASIN_OIL_COUNT = "OilCount";

    public static final String OIL_POT_OIL_COUNT = "OilCount";

    public static final String SHAWARMA_SPIT_COOKING_ITEM = "CookingItem";
    public static final String SHAWARMA_SPIT_COOKED_ITEM = "CookedItem";
    public static final String SHAWARMA_SPIT_COOK_TIME = "CookTime";

    public static final String TRASH_CAN_ITEMS = "TrashCanItems";

    public static final String KITCHENWARE_RACKS_LEFT_ITEM = "LeftItem";
    public static final String KITCHENWARE_RACKS_RIGHT_ITEM = "RightItem";

    public static final class PotStatus {
        public static final int PUT_INGREDIENT = 0;
        public static final int COOKING = 1;
        public static final int FINISHED = 2;
        public static final int BURNT = 3;

        private PotStatus() {}
    }

    public static final class StockpotStatus {
        public static final int PUT_SOUP_BASE = 0;
        public static final int PUT_INGREDIENT = 1;
        public static final int COOKING = 2;
        public static final int FINISHED = 3;

        private StockpotStatus() {}
    }

    public static final class TeapotStatus {
        public static final int PUT_INGREDIENT = 0;
        public static final int PROCESSING = 1;
        public static final int FINISHED = 2;

        private TeapotStatus() {}
    }
}
