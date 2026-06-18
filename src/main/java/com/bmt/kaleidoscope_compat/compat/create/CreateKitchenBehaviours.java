package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.interaction.*;
import com.bmt.kaleidoscope_compat.compat.create.movement.*;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
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
        BEHAVIOUR_MAP.put(ModBlocks.POT.get(), new BehaviourPair(
                PotMovementBehaviour::new, PotMovingInteraction::new));

        // 灶台
        BEHAVIOUR_MAP.put(ModBlocks.STOVE.get(), new BehaviourPair(
                StoveMovementBehaviour::new, StoveMovingInteraction::new));

        // 汤锅
        BEHAVIOUR_MAP.put(ModBlocks.STOCKPOT.get(), new BehaviourPair(
                StockpotMovementBehaviour::new, StockpotMovingInteraction::new));

        // 蒸笼
        BEHAVIOUR_MAP.put(ModBlocks.STEAMER.get(), new BehaviourPair(
                SteamerMovementBehaviour::new, SteamerMovingInteraction::new));

        // 茶壶
        BEHAVIOUR_MAP.put(ModBlocks.TEAPOT.get(), new BehaviourPair(
                TeapotMovementBehaviour::new, TeapotMovingInteraction::new));

        //水果篮
        BEHAVIOUR_MAP.put(ModBlocks.FRUIT_BASKET.get(), new BehaviourPair(
                FruitBasketMovementBehaviour::new, FruitBasketMovingInteraction::new));

        // 砧板
        BEHAVIOUR_MAP.put(ModBlocks.CHOPPING_BOARD.get(), new BehaviourPair(
                ChoppingBoardMovementBehaviour::new, ChoppingBoardMovingInteraction::new));

        // 搪瓷盆
        BEHAVIOUR_MAP.put(ModBlocks.ENAMEL_BASIN.get(), new BehaviourPair(
                EnamelBasinMovementBehaviour::new, EnamelBasinMovingInteraction::new));

        // 厨具架
        BEHAVIOUR_MAP.put(ModBlocks.KITCHENWARE_RACKS.get(), new BehaviourPair(
                KitchenwareRacksMovementBehaviour::new, KitchenwareRacksMovingInteraction::new));

        // 石磨
        BEHAVIOUR_MAP.put(ModBlocks.MILLSTONE.get(), new BehaviourPair(
                MillstoneMovementBehaviour::new, MillstoneMovingInteraction::new));

        // 油壶
        BEHAVIOUR_MAP.put(ModBlocks.OIL_POT.get(), new BehaviourPair(
                OilPotMovementBehaviour::new, OilPotMovingInteraction::new));

        // 烤肉架
        BEHAVIOUR_MAP.put(ModBlocks.SHAWARMA_SPIT.get(), new BehaviourPair(
                ShawarmaSpitMovementBehaviour::new, ShawarmaSpitMovingInteraction::new));

        // 垃圾桶
        BEHAVIOUR_MAP.put(ModBlocks.TRASH_CAN.get(), new BehaviourPair(
                TrashCanMovementBehaviour::new, TrashCanMovingInteraction::new));
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
