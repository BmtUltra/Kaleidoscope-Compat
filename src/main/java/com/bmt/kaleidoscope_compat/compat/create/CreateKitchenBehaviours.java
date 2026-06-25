package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.interaction.*;
import com.bmt.kaleidoscope_compat.compat.create.movement.*;
import com.bmt.kaleidoscope_compat.config.category.contraption.ContraptionConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 注册森罗厨房方块在 Create 动态结构上的行为
 */
public class CreateKitchenBehaviours {
    private static boolean initialized = false;

    private static final Map<Block, BehaviourPair> BEHAVIOUR_MAP = new HashMap<>();

    static {
        // 炒锅
        if (ContraptionConfig.potEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.POT.get(), new BehaviourPair(
                    PotMovementBehaviour::new, PotMovingInteraction::new));
        }

        // 灶台
        if (ContraptionConfig.stoveEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.STOVE.get(), new BehaviourPair(
                    StoveMovementBehaviour::new, StoveMovingInteraction::new));
        }

        // 汤锅
        if (ContraptionConfig.stockpotEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.STOCKPOT.get(), new BehaviourPair(
                    StockpotMovementBehaviour::new, StockpotMovingInteraction::new));
        }

        // 蒸笼
        if (ContraptionConfig.steamerEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.STEAMER.get(), new BehaviourPair(
                    SteamerMovementBehaviour::new, SteamerMovingInteraction::new));
        }

        // 茶壶
        if (ContraptionConfig.teapotEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.TEAPOT.get(), new BehaviourPair(
                    TeapotMovementBehaviour::new, TeapotMovingInteraction::new));
        }

        //水果篮
        if (ContraptionConfig.fruitBasketEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.FRUIT_BASKET.get(), new BehaviourPair(
                    FruitBasketMovementBehaviour::new, FruitBasketMovingInteraction::new));
        }

        // 砧板
        if (ContraptionConfig.choppingBoardEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.CHOPPING_BOARD.get(), new BehaviourPair(
                    ChoppingBoardMovementBehaviour::new, ChoppingBoardMovingInteraction::new));
        }

        // 搪瓷盆
        if (ContraptionConfig.enamelBasinEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.ENAMEL_BASIN.get(), new BehaviourPair(
                    EnamelBasinMovementBehaviour::new, EnamelBasinMovingInteraction::new));
        }

        // 厨具架
        if (ContraptionConfig.kitchenwareRacksEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.KITCHENWARE_RACKS.get(), new BehaviourPair(
                    KitchenwareRacksMovementBehaviour::new, KitchenwareRacksMovingInteraction::new));
        }

        // 石磨
        if (ContraptionConfig.millstoneEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.MILLSTONE.get(), new BehaviourPair(
                    MillstoneMovementBehaviour::new, MillstoneMovingInteraction::new));
        }

        // 油壶
        if (ContraptionConfig.oilPotEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.OIL_POT.get(), new BehaviourPair(
                    OilPotMovementBehaviour::new, OilPotMovingInteraction::new));
        }

        // 烤肉架
        if (ContraptionConfig.shawarmaSpitEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.SHAWARMA_SPIT.get(), new BehaviourPair(
                    ShawarmaSpitMovementBehaviour::new, ShawarmaSpitMovingInteraction::new));
        }

        // 垃圾桶
        if (ContraptionConfig.trashCanEnabled) {
            BEHAVIOUR_MAP.put(ModBlocks.TRASH_CAN.get(), new BehaviourPair(
                    TrashCanMovementBehaviour::new, TrashCanMovingInteraction::new));
        }
    }

    public static void register() {
        if (initialized) return;


        for (Map.Entry<Block, BehaviourPair> entry : BEHAVIOUR_MAP.entrySet()) {
            Block block = entry.getKey();
            BehaviourPair pair = entry.getValue();

            if (pair.movement() != null) {
                MovementBehaviour.REGISTRY.register(block, pair.movement().get());
            }

            if (pair.interaction() != null) {
                MovingInteractionBehaviour.REGISTRY.register(block, pair.interaction().get());
            }
        }

        // 桌子（11 木种）：物品存储 + 地毯
        if (ContraptionConfig.tableEnabled) {
            Block[] tables = {
                    ModBlocks.TABLE_OAK.get(), ModBlocks.TABLE_SPRUCE.get(), ModBlocks.TABLE_ACACIA.get(),
                    ModBlocks.TABLE_BAMBOO.get(), ModBlocks.TABLE_BIRCH.get(), ModBlocks.TABLE_CHERRY.get(),
                    ModBlocks.TABLE_CRIMSON.get(), ModBlocks.TABLE_DARK_OAK.get(), ModBlocks.TABLE_JUNGLE.get(),
                    ModBlocks.TABLE_MANGROVE.get(), ModBlocks.TABLE_WARPED.get()
            };
            for (Block block : tables) {
                MovementBehaviour.REGISTRY.register(block, new TableMovementBehaviour());
                MovingInteractionBehaviour.REGISTRY.register(block, new TableMovingInteraction());
            }
        }

        // 椅子（11 木种）：坐具 + 地毯
        if (ContraptionConfig.chairEnabled) {
            Block[] chairs = {
                    ModBlocks.CHAIR_OAK.get(), ModBlocks.CHAIR_SPRUCE.get(), ModBlocks.CHAIR_ACACIA.get(),
                    ModBlocks.CHAIR_BAMBOO.get(), ModBlocks.CHAIR_BIRCH.get(), ModBlocks.CHAIR_CHERRY.get(),
                    ModBlocks.CHAIR_CRIMSON.get(), ModBlocks.CHAIR_DARK_OAK.get(), ModBlocks.CHAIR_JUNGLE.get(),
                    ModBlocks.CHAIR_MANGROVE.get(), ModBlocks.CHAIR_WARPED.get()
            };
            for (Block block : chairs) {
                MovementBehaviour.REGISTRY.register(block, new ChairMovementBehaviour());
                MovingInteractionBehaviour.REGISTRY.register(block, new ChairMovingInteraction());
            }
        }

        // 烹饪凳（11 木种）：坐具
        if (ContraptionConfig.cookStoolEnabled) {
            Block[] stools = {
                    ModBlocks.COOK_STOOL_OAK.get(), ModBlocks.COOK_STOOL_SPRUCE.get(), ModBlocks.COOK_STOOL_ACACIA.get(),
                    ModBlocks.COOK_STOOL_BAMBOO.get(), ModBlocks.COOK_STOOL_BIRCH.get(), ModBlocks.COOK_STOOL_CHERRY.get(),
                    ModBlocks.COOK_STOOL_CRIMSON.get(), ModBlocks.COOK_STOOL_DARK_OAK.get(), ModBlocks.COOK_STOOL_JUNGLE.get(),
                    ModBlocks.COOK_STOOL_MANGROVE.get(), ModBlocks.COOK_STOOL_WARPED.get()
            };
            for (Block block : stools) {
                MovementBehaviour.REGISTRY.register(block, new CookStoolMovementBehaviour());
                MovingInteractionBehaviour.REGISTRY.register(block, new CookStoolMovingInteraction());
            }
        }

        // 将烤肉架注册为"脆性方块"，使其在动态结构拆卸时跳过 updateShape 预检查，
        // 避免双层方块（UPPER/LOWER）因邻居半块未放置而被 updateShape 转为空气导致烤肉塔方块消失。
        BlockMovementChecks.registerBrittleCheck(state -> {
            if (state.getBlock() instanceof ShawarmaSpitBlock) {
                return CheckResult.SUCCESS;
            }
            return CheckResult.PASS;
        });

        initialized = true;
    }

    /**
     * 行为对：MovementBehaviour + MovingInteractionBehaviour
     */
    private record BehaviourPair(
            Supplier<MovementBehaviour> movement,
            Supplier<MovingInteractionBehaviour> interaction
    ) {}
}
