package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HALF;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HAS_LID;

/**
 * 蒸笼在动态结构上的移动行为
 */
public class SteamerMovementBehaviour extends BaseMovementBehaviour {

    private static final int MAX_LIT_LEVEL = 4;
    private static final int SLOT_COUNT = 8;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof SteamerBlock;
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (context.world.getGameTime() % 5 == 0) {
            int litLevel = calculateLitLevel(context);
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(STEAMER_LIT_LEVEL, litLevel);
            updateNbt(context, newNbt);

            // 检查是否有蒸熟的物品，释放熟粒粒子
            int[] cookingTime = nbt.contains(STEAMER_COOKING_TIME, Tag.TAG_INT_ARRAY) ? nbt.getIntArray(STEAMER_COOKING_TIME) : new int[SLOT_COUNT];
            for (int j : cookingTime) {
                if (j == -1) {
                    makeRipeParticles(context, state);
                    break;
                }
            }
        }

        int litLevel = nbt.getInt(STEAMER_LIT_LEVEL);
        if (litLevel > 0) {
            cookingTick(context, state, nbt);
        } else {
            cooldownTick(context, state, nbt);
        }
    }

    /**
     * 计算火力等级
     * - 下方是热源 = 4 级
     * - 下方是双层蒸笼 = 下方 litLevel - 1
     * - 下方是单层蒸笼 = 0 级
     */
    private int calculateLitLevel(MovementContext context) {
        Map<BlockPos, StructureBlockInfo> blocks = context.contraption.getBlocks();
        BlockPos belowPos = context.localPos.below();
        StructureBlockInfo belowInfo = blocks.get(belowPos);

        if (belowInfo != null && belowInfo.state().is(ModBlocks.STEAMER.get())) {
            BlockState belowState = belowInfo.state();

            if (belowState.getValue(HALF)) {
                return 0;
            }
            CompoundTag belowNbt = belowInfo.nbt();
            if (belowNbt != null) {
                int belowLitLevel = belowNbt.getInt(STEAMER_LIT_LEVEL);
                return Math.max(belowLitLevel - 1, 0);
            }
            return 0;
        }

        if (hasDirectHeatSource(context)) {
            return MAX_LIT_LEVEL;
        }

        return 0;
    }

    private boolean hasDirectHeatSource(MovementContext context) {
        Map<BlockPos, StructureBlockInfo> blocks = context.contraption.getBlocks();
        BlockPos belowPos = context.localPos.below();
        StructureBlockInfo belowInfo = blocks.get(belowPos);

        if (belowInfo != null) {
            BlockState belowState = belowInfo.state();
            if (belowState.hasProperty(BlockStateProperties.LIT)) {
                return belowState.getValue(BlockStateProperties.LIT);
            }
            return belowState.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
        }
        return false;
    }

    /**
     * 检查上方是否是蒸笼
     */
    private boolean isAboveSteamer(MovementContext context) {
        Map<BlockPos, StructureBlockInfo> blocks = context.contraption.getBlocks();
        StructureBlockInfo aboveInfo = blocks.get(context.localPos.above());
        return aboveInfo != null && aboveInfo.state().is(ModBlocks.STEAMER.get());
    }

    private void cookingTick(MovementContext context, BlockState state, CompoundTag nbt) {
        RegistryAccess registryAccess = context.world.registryAccess();
        NonNullList<ItemStack> items = ContraptionUtil.readItems(nbt, registryAccess, SLOT_COUNT);
        int[] cookingProgress = nbt.contains(STEAMER_COOKING_PROGRESS, Tag.TAG_INT_ARRAY) ? nbt.getIntArray(STEAMER_COOKING_PROGRESS) : new int[SLOT_COUNT];
        int[] cookingTime = nbt.contains(STEAMER_COOKING_TIME, Tag.TAG_INT_ARRAY) ? nbt.getIntArray(STEAMER_COOKING_TIME) : new int[SLOT_COUNT];
        boolean half = state.getValue(HALF);
        int endIndex = half ? 4 : 8;

        boolean aboveIsSteamer = isAboveSteamer(context);

        if (!aboveIsSteamer) {
            makeCookingParticles(context, state);
            if (!state.getValue(HAS_LID)) {
                return;
            }
        }

        boolean changed = false;

        for (int i = 0; i < endIndex; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty())
                continue;

            if (cookingTime[i] == -1)
                continue;

            changed = true;
            cookingProgress[i]++;

            if (cookingProgress[i] >= cookingTime[i]) {
                SingleRecipeInput input = new SingleRecipeInput(stack);
                var recipe = context.world.getRecipeManager().getRecipeFor(ModRecipes.STEAMER_RECIPE, input,
                        context.world);
                ItemStack resultStack = recipe.map(r -> r.value().assemble(input, registryAccess)).orElse(stack);

                if (!resultStack.isEmpty()) {
                    items.set(i, resultStack);
                    cookingTime[i] = -1;
                }
            }
        }

        if (changed) {
            CompoundTag newNbt = nbt.copy();
            ContainerHelper.saveAllItems(newNbt, items, true, registryAccess);
            newNbt.putIntArray(STEAMER_COOKING_PROGRESS, cookingProgress);
            newNbt.putIntArray(STEAMER_COOKING_TIME, cookingTime);
            updateNbt(context, newNbt);
        }
    }

    private void cooldownTick(MovementContext context, BlockState state, CompoundTag nbt) {
        int[] cookingProgress = nbt.contains(STEAMER_COOKING_PROGRESS, Tag.TAG_INT_ARRAY) ? nbt.getIntArray(STEAMER_COOKING_PROGRESS) : new int[SLOT_COUNT];
        int[] cookingTime = nbt.contains(STEAMER_COOKING_TIME, Tag.TAG_INT_ARRAY) ? nbt.getIntArray(STEAMER_COOKING_TIME) : new int[SLOT_COUNT];
        boolean half = state.getValue(HALF);
        int endIndex = half ? 4 : 8;
        boolean changed = false;

        for (int i = 0; i < endIndex; i++) {
            if (cookingProgress[i] > 0) {
                changed = true;
                cookingProgress[i] = Mth.clamp(cookingProgress[i] - 2, 0, cookingTime[i]);
            }
        }

        if (changed) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putIntArray(STEAMER_COOKING_PROGRESS, cookingProgress);
            newNbt.putIntArray(STEAMER_COOKING_TIME, cookingTime);
            updateNbt(context, newNbt);
        }
    }

    /**
     * 烹饪粒子：蒸笼正在烹饪时产生的蒸汽
     */
    private void makeCookingParticles(MovementContext context, BlockState state) {
        if (!(context.world instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (serverLevel.random.nextFloat() >= 0.1F) return;

        boolean half = state.getValue(HALF);
        double yOffset = half ? 0.5 : 1.0;
        net.minecraft.util.RandomSource random = serverLevel.random;

        Vec3 globalPos = context.contraption.entity.toGlobalVector(
                net.minecraft.world.phys.Vec3.atCenterOf(context.localPos), 1.0f);

        serverLevel.sendParticles(ModParticles.COOKING.get(),
                globalPos.x + random.nextDouble() / 2 * (random.nextBoolean() ? 1 : -1),
                globalPos.y - 0.5 + yOffset + random.nextDouble() / 2,
                globalPos.z + random.nextDouble() / 2 * (random.nextBoolean() ? 1 : -1),
                1, 0, 0, 0, 0.05);
    }

    /**
     * 熟粒粒子：食物蒸熟后产生的粒子
     */
    private void makeRipeParticles(MovementContext context, BlockState state) {
        if (!(context.world instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (serverLevel.random.nextFloat() >= 0.5F) return;

        boolean half = state.getValue(HALF);
        double yOffset = half ? 0.25 : 0.75;
        net.minecraft.util.RandomSource random = serverLevel.random;

        Vec3 globalPos = context.contraption.entity.toGlobalVector(
                net.minecraft.world.phys.Vec3.atCenterOf(context.localPos), 1.0f);

        serverLevel.sendParticles(ModParticles.COOKING.get(),
                globalPos.x + random.nextDouble() / 1.25 * (random.nextBoolean() ? 1 : -1),
                globalPos.y - 0.5 + yOffset + random.nextDouble() / 2,
                globalPos.z + random.nextDouble() / 1.25 * (random.nextBoolean() ? 1 : -1),
                1, 0, 0, 0, 0.05);
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        NonNullList<ItemStack> items = ContraptionUtil.readItems(nbt, context.world.registryAccess(), SLOT_COUNT);
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }
}
