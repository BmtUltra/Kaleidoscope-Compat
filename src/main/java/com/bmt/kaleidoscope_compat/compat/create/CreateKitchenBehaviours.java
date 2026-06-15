package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.interaction.*;
import com.bmt.kaleidoscope_compat.compat.create.movement.PotMovementBehaviour;
import com.bmt.kaleidoscope_compat.compat.create.movement.SteamerMovementBehaviour;
import com.bmt.kaleidoscope_compat.compat.create.movement.StockpotMovementBehaviour;
import com.bmt.kaleidoscope_compat.compat.create.movement.TeapotMovementBehaviour;
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
                null, StoveMovingInteraction::new));

        // 汤锅
        BEHAVIOUR_MAP.put(ModBlocks.STOCKPOT.get(), new BehaviourPair(
                StockpotMovementBehaviour::new, StockpotMovingInteraction::new));

        // 蒸笼
        BEHAVIOUR_MAP.put(ModBlocks.STEAMER.get(), new BehaviourPair(
                SteamerMovementBehaviour::new, SteamerMovingInteraction::new));

        // 茶壶
        BEHAVIOUR_MAP.put(ModBlocks.TEAPOT.get(), new BehaviourPair(
                TeapotMovementBehaviour::new, TeapotMovingInteraction::new));
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
